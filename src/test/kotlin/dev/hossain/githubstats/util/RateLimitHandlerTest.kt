package dev.hossain.githubstats.util

import com.google.common.truth.Truth.assertThat
import okhttp3.Headers
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import retrofit2.Response
import java.time.Instant

/**
 * Test for [RateLimitHandler]
 */
class RateLimitHandlerTest {
    private lateinit var rateLimitHandler: RateLimitHandler

    @BeforeEach
    fun setUp() {
        rateLimitHandler = RateLimitHandler()
    }

    @Test
    fun `calculateDelay - given null response - returns minimum delay`() {
        val delay = rateLimitHandler.calculateDelay(null)

        assertThat(delay).isEqualTo(1000L)
    }

    @Test
    fun `calculateDelay - given response with plenty of remaining requests - returns minimum delay`() {
        val headers =
            Headers
                .Builder()
                .add("X-RateLimit-Remaining", "4000")
                .add("X-RateLimit-Limit", "5000")
                .add("X-RateLimit-Reset", "${Instant.now().epochSecond + 3600}")
                .build()
        val response = Response.success("", headers)

        val delay = rateLimitHandler.calculateDelay(response)

        assertThat(delay).isEqualTo(1000L)
    }

    @Test
    fun `calculateDelay - given response with low remaining requests - returns calculated delay`() {
        val resetTime = Instant.now().epochSecond + 3600 // 1 hour from now
        val headers =
            Headers
                .Builder()
                .add("X-RateLimit-Remaining", "100")
                .add("X-RateLimit-Limit", "5000")
                .add("X-RateLimit-Reset", resetTime.toString())
                .build()
        val response = Response.success("", headers)

        val delay = rateLimitHandler.calculateDelay(response)

        // Should calculate delay to spread remaining requests over time
        assertThat(delay).isGreaterThan(1000L)
        assertThat(delay).isLessThan(60000L) // Should be capped at 1 minute
    }

    @Test
    fun `calculateRetryDelay - given attempt numbers - returns exponential backoff delays`() {
        val delay1 = rateLimitHandler.calculateRetryDelay(1)
        val delay2 = rateLimitHandler.calculateRetryDelay(2)
        val delay3 = rateLimitHandler.calculateRetryDelay(3)

        assertThat(delay1).isEqualTo(1000L) // 1 second
        assertThat(delay2).isEqualTo(2000L) // 2 seconds
        assertThat(delay3).isEqualTo(4000L) // 4 seconds
    }

    @Test
    fun `calculateRetryDelay - given high attempt number - returns capped delay`() {
        val delay = rateLimitHandler.calculateRetryDelay(20)

        assertThat(delay).isEqualTo(30 * 60 * 1000L) // Should be capped at 30 minutes
    }

    @Test
    fun `calculateResetDelay - given response with future reset time - returns delay until reset`() {
        val resetTime = Instant.now().epochSecond + 120 // 2 minutes from now
        val headers =
            Headers
                .Builder()
                .add("X-RateLimit-Reset", resetTime.toString())
                .build()
        val response = Response.success("", headers)

        val delay = rateLimitHandler.calculateResetDelay(response)

        assertThat(delay).isGreaterThan(110000L) // Should be around 2 minutes
        assertThat(delay).isLessThan(130000L)
    }

    @Test
    fun `calculateResetDelay - given response with past reset time - returns null`() {
        val resetTime = Instant.now().epochSecond - 60 // 1 minute ago
        val headers =
            Headers
                .Builder()
                .add("X-RateLimit-Reset", resetTime.toString())
                .build()
        val response = Response.success("", headers)

        val delay = rateLimitHandler.calculateResetDelay(response)

        assertThat(delay).isNull()
    }

    @Test
    fun `calculateResetDelay - given response without reset header - returns null`() {
        val headers = Headers.Builder().build()
        val response = Response.success("", headers)

        val delay = rateLimitHandler.calculateResetDelay(response)

        assertThat(delay).isNull()
    }

    @Test
    fun `isRateLimitError - given rate limit error info - returns true`() {
        val githubError =
            GithubError(
                message = "API rate limit exceeded for user ID 99822",
                documentationUrl = "https://docs.github.com/rest/overview/rate-limits-for-the-rest-api",
                status = 403,
            )
        val errorInfo =
            ErrorInfo(
                errorMessage = "Rate limit exceeded",
                exception = Exception(),
                githubError = githubError,
            )

        val isRateLimit = rateLimitHandler.isRateLimitError(errorInfo)

        assertThat(isRateLimit).isTrue()
    }

    @Test
    fun `isRateLimitError - given non-rate limit error info - returns false`() {
        val githubError =
            GithubError(
                message = "Not Found",
                documentationUrl = "https://docs.github.com/rest",
                status = 404,
            )
        val errorInfo =
            ErrorInfo(
                errorMessage = "Not found",
                exception = Exception(),
                githubError = githubError,
            )

        val isRateLimit = rateLimitHandler.isRateLimitError(errorInfo)

        assertThat(isRateLimit).isFalse()
    }

    @Test
    fun `isRateLimitError - given error info without github error - returns false`() {
        val errorInfo =
            ErrorInfo(
                errorMessage = "Some error",
                exception = Exception(),
            )

        val isRateLimit = rateLimitHandler.isRateLimitError(errorInfo)

        assertThat(isRateLimit).isFalse()
    }
}
