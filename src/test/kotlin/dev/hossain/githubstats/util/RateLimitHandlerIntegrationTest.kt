package dev.hossain.githubstats.util

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import okhttp3.Headers
import org.junit.jupiter.api.Test
import retrofit2.Response

/**
 * Integration test for RateLimitHandler to ensure it works correctly with real API calls.
 */
class RateLimitHandlerIntegrationTest {

    @Test
    fun `executeWithRateLimit - given successful API call - returns result without retry`() = runTest {
        val rateLimitHandler = RateLimitHandler()
        val errorProcessor = ErrorProcessor()
        var callCount = 0

        val result = rateLimitHandler.executeWithRateLimit(
            apiCall = {
                callCount++
                "success"
            },
            errorProcessor = errorProcessor,
        )

        assertThat(result).isEqualTo("success")
        assertThat(callCount).isEqualTo(1) // Should only call once for successful call
    }

    @Test
    fun `calculateDelay - with real rate limit headers - calculates appropriate delays`() {
        val rateLimitHandler = RateLimitHandler()

        // Test with plenty of requests remaining
        val abundantHeaders = Headers
            .Builder()
            .add("X-RateLimit-Remaining", "4000")
            .add("X-RateLimit-Limit", "5000")
            .add("X-RateLimit-Reset", "${System.currentTimeMillis() / 1000 + 3600}")
            .build()
        val abundantResponse = Response.success("", abundantHeaders)

        val abundantDelay = rateLimitHandler.calculateDelay(abundantResponse)
        assertThat(abundantDelay).isEqualTo(1000L) // Should use minimum delay

        // Test with low requests remaining
        val lowHeaders = Headers
            .Builder()
            .add("X-RateLimit-Remaining", "50")
            .add("X-RateLimit-Limit", "5000")
            .add("X-RateLimit-Reset", "${System.currentTimeMillis() / 1000 + 1800}") // 30 minutes
            .build()
        val lowResponse = Response.success("", lowHeaders)

        val lowDelay = rateLimitHandler.calculateDelay(lowResponse)
        assertThat(lowDelay).isGreaterThan(1000L) // Should use calculated delay
        assertThat(lowDelay).isLessThan(60000L) // Should be capped at 1 minute
    }

    @Test
    fun `isRateLimitError - correctly identifies rate limit errors`() {
        val rateLimitHandler = RateLimitHandler()

        // Test with actual rate limit error
        val rateLimitError = GithubError(
            message = "API rate limit exceeded for user ID 99822. If you reach out to GitHub Support for help, please include the request ID CF05:4DFD1:19AA6E5:33F8C4B:687A2E8E and timestamp 2025-07-18 11:22:54 UTC.",
            documentationUrl = "https://docs.github.com/rest/overview/rate-limits-for-the-rest-api",
            status = 403,
        )
        val rateLimitErrorInfo = ErrorInfo(
            errorMessage = "Rate limit exceeded",
            exception = Exception(),
            githubError = rateLimitError,
        )

        assertThat(rateLimitHandler.isRateLimitError(rateLimitErrorInfo)).isTrue()

        // Test with non-rate-limit error
        val otherError = GithubError(
            message = "Not Found",
            documentationUrl = "https://docs.github.com/rest",
            status = 404,
        )
        val otherErrorInfo = ErrorInfo(
            errorMessage = "Not found",
            exception = Exception(),
            githubError = otherError,
        )

        assertThat(rateLimitHandler.isRateLimitError(otherErrorInfo)).isFalse()
    }
}