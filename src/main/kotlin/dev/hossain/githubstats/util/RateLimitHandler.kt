package dev.hossain.githubstats.util

import dev.hossain.githubstats.logging.Log
import kotlinx.coroutines.delay
import retrofit2.Response
import java.time.Instant
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

/**
 * Handles GitHub API rate limiting with smart delay calculations and retry logic.
 */
class RateLimitHandler {
    companion object {
        /**
         * Minimum delay between API requests (1 second).
         */
        private const val MIN_REQUEST_DELAY_MS = 1000L

        /**
         * Maximum delay for exponential backoff (30 minutes).
         */
        private const val MAX_BACKOFF_DELAY_MS = 30 * 60 * 1000L

        /**
         * Base delay for exponential backoff calculation (1 second).
         */
        private const val BASE_BACKOFF_DELAY_MS = 1000L

        /**
         * Maximum number of retry attempts for rate limit errors.
         */
        private const val MAX_RETRY_ATTEMPTS = 5

        /**
         * GitHub rate limit header for remaining requests.
         */
        private const val HEADER_RATE_LIMIT_REMAINING = "X-RateLimit-Remaining"

        /**
         * GitHub rate limit header for reset time (Unix timestamp).
         */
        private const val HEADER_RATE_LIMIT_RESET = "X-RateLimit-Reset"

        /**
         * GitHub rate limit header for total limit.
         */
        private const val HEADER_RATE_LIMIT_LIMIT = "X-RateLimit-Limit"

        /**
         * Maximum delay to spread requests when low on rate limit (1 minute).
         */
        private const val MAX_SPREAD_DELAY_MS = 60000L

        /**
         * Default GitHub rate limit if header is missing (per hour).
         */
        private const val DEFAULT_RATE_LIMIT = 5000
    }

    /**
     * Calculates appropriate delay based on current rate limit status.
     * Uses rate limit headers from the last response to determine optimal delay.
     *
     * @param lastResponse The last HTTP response containing rate limit headers
     * @return Delay in milliseconds before next request
     */
    fun calculateDelay(lastResponse: Response<*>?): Long {
        if (lastResponse == null) {
            return MIN_REQUEST_DELAY_MS
        }

        val remaining = lastResponse.headers()[HEADER_RATE_LIMIT_REMAINING]?.toIntOrNull() ?: return MIN_REQUEST_DELAY_MS
        val resetTime = lastResponse.headers()[HEADER_RATE_LIMIT_RESET]?.toLongOrNull()
        val limit = lastResponse.headers()[HEADER_RATE_LIMIT_LIMIT]?.toIntOrNull() ?: DEFAULT_RATE_LIMIT

        // If we have plenty of requests remaining, use minimum delay
        if (remaining > limit * 0.1) { // More than 10% remaining
            return MIN_REQUEST_DELAY_MS
        }

        // If we're running low on requests, calculate delay to spread them over remaining time
        if (resetTime != null) {
            val currentTime = Instant.now().epochSecond
            val timeUntilReset = resetTime - currentTime

            if (timeUntilReset > 0 && remaining > 0) {
                // Spread remaining requests over time until reset
                val delayMs = (timeUntilReset * 1000L) / remaining
                return max(MIN_REQUEST_DELAY_MS, min(delayMs, MAX_SPREAD_DELAY_MS)) // Cap at 1 minute
            }
        }

        // Conservative fallback - 2 second delay when we're low on requests
        return 2000L
    }

    /**
     * Calculates exponential backoff delay for retry attempts.
     *
     * @param attemptNumber The current retry attempt number (starting from 1)
     * @return Delay in milliseconds before retry
     */
    fun calculateRetryDelay(attemptNumber: Int): Long {
        val delay = BASE_BACKOFF_DELAY_MS * (2.0.pow(attemptNumber - 1)).toLong()
        return min(delay, MAX_BACKOFF_DELAY_MS)
    }

    /**
     * Calculates delay until rate limit reset based on headers.
     *
     * @param response The HTTP response containing rate limit headers
     * @return Delay in milliseconds until rate limit resets, or null if not available
     */
    fun calculateResetDelay(response: Response<*>): Long? =
        response.headers()[HEADER_RATE_LIMIT_RESET]?.toLongOrNull()?.let { resetTime ->
            val currentTime = Instant.now().epochSecond
            val timeUntilReset = resetTime - currentTime
            if (timeUntilReset > 0) {
                min(timeUntilReset * 1000L, MAX_BACKOFF_DELAY_MS)
            } else {
                null
            }
        }

    /**
     * Determines if the error is a rate limit error that should be retried.
     *
     * @param errorInfo The error information to check
     * @return True if this is a rate limit error that can be retried
     */
    fun isRateLimitError(errorInfo: ErrorInfo) =
        errorInfo.githubError?.message?.contains("API rate limit exceeded") == true ||
            errorInfo.githubError?.message?.contains("rate limit") == true

    /**
     * Executes an API call with smart rate limiting and retry logic.
     *
     * @param T The return type of the API call
     * @param apiCall The suspend function to execute
     * @param errorProcessor Error processor for handling exceptions
     * @param lastResponse Optional last response for rate limit header information
     * @return The result of the API call
     * @throws Exception If all retry attempts fail or a non-retryable error occurs
     */
    suspend fun <T> executeWithRateLimit(
        apiCall: suspend () -> T,
        errorProcessor: ErrorProcessor,
        lastResponse: Response<*>? = null,
    ): T {
        var attemptNumber = 1

        while (attemptNumber <= MAX_RETRY_ATTEMPTS) {
            try {
                // Apply delay before making the request (except for first attempt)
                if (attemptNumber > 1) {
                    val retryDelay = calculateRetryDelay(attemptNumber - 1)
                    Log.w("🔄 Rate limited. Retrying in ${retryDelay / 1000} seconds (attempt $attemptNumber/$MAX_RETRY_ATTEMPTS)")
                    delay(retryDelay)
                } else if (lastResponse != null) {
                    val requestDelay = calculateDelay(lastResponse)
                    delay(requestDelay)
                }

                return apiCall()
            } catch (exception: Exception) {
                val errorInfo = errorProcessor.getDetailedError(exception)

                if (isRateLimitError(errorInfo)) {
                    if (attemptNumber >= MAX_RETRY_ATTEMPTS) {
                        Log.w("❌ Rate limit retry attempts exhausted. Failing after $MAX_RETRY_ATTEMPTS attempts.")
                        throw errorInfo.exception
                    }

                    // If we have rate limit reset information, use it
                    if (exception is retrofit2.HttpException) {
                        val response = exception.response()
                        val resetDelay = response?.let { calculateResetDelay(it) }
                        if (resetDelay != null && resetDelay < MAX_BACKOFF_DELAY_MS) {
                            Log.w("⏰ Rate limit exceeded. Waiting ${resetDelay / 1000} seconds until reset...")
                            delay(resetDelay + 1000L) // Add 1 second buffer
                            attemptNumber++ // This counts as a retry attempt
                            continue
                        }
                    }
                    attemptNumber++
                    continue
                } else {
                    // Non-rate-limit error, don't retry
                    throw errorInfo.exception
                }
            }
        }
        throw IllegalStateException("Unexpected state in rate limit retry logic")
    }
}
