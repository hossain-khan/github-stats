package dev.hossain.githubstats.util

import com.google.common.truth.Truth.assertThat
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import retrofit2.HttpException
import retrofit2.Response

/**
 * Test for [ErrorProcessor]
 */
class ErrorProcessorTest {
    @BeforeEach
    fun setUp() {
    }

    @AfterEach
    fun tearDown() {
    }

    @Test
    fun `getDetailedError - given HttpException with error body - returns ErrorInfo processed data`() {
        val httpException = HttpException(Response.error<Any>(404, "Not found".toResponseBody("text/plain".toMediaTypeOrNull())))
        val errorProcessor = ErrorProcessor()

        val errorInfo = errorProcessor.getDetailedError(httpException)

        assertThat(errorInfo).isInstanceOf(ErrorInfo::class.java)
        assertThat(errorInfo.errorMessage).contains("HTTP 404")
        assertThat(errorInfo.errorMessage).contains("Not found")
    }

    @Test
    fun `getDetailedError - given HttpException without error body - returns ErrorInfo processed data`() {
        val httpException = HttpException(Response.error<Any>(404, "".toResponseBody("text/plain".toMediaTypeOrNull())))
        val errorProcessor = ErrorProcessor()

        val errorInfo = errorProcessor.getDetailedError(httpException)

        assertThat(errorInfo).isInstanceOf(ErrorInfo::class.java)
        assertThat(errorInfo.errorMessage).contains("HTTP 404")
    }

    @Test
    fun `getDetailedError - given non HttpException - returns ErrorInfo processed data`() {
        val exception = Exception("Some error")
        val errorProcessor = ErrorProcessor()

        val errorInfo = errorProcessor.getDetailedError(exception)

        assertThat(errorInfo).isInstanceOf(ErrorInfo::class.java)
        assertThat(errorInfo.errorMessage).contains("Some error")
    }

    @Test
    fun `getDetailedError - given HttpException with JSON error body - returns ErrorInfo processed data`() {
        val jsonErrorBody = """{"message":"Bad credentials","documentation_url":"https://docs.github.com/rest"}"""
        val httpException = HttpException(Response.error<Any>(401, jsonErrorBody.toResponseBody("application/json".toMediaTypeOrNull())))
        val errorProcessor = ErrorProcessor()

        val errorInfo = errorProcessor.getDetailedError(httpException)

        assertThat(errorInfo).isInstanceOf(ErrorInfo::class.java)
        assertThat(errorInfo.errorMessage).contains("HTTP 401")
        assertThat(errorInfo.errorMessage).contains("Bad credentials")
        assertThat(errorInfo.githubError?.message).isEqualTo("Bad credentials")
    }

    @Test
    fun `getDetailedError - given HttpException with JSON error body and code - returns ErrorInfo processed data`() {
        // language=JSON
        val jsonErrorBody =
            """
            {
              "message": "Not Found",
              "documentation_url": "https://docs.github.com/rest/pulls/pulls#get-a-pull-request",
              "status": "404"
            }
            """.trimIndent()
        val httpException = HttpException(Response.error<Any>(404, jsonErrorBody.toResponseBody("application/json".toMediaTypeOrNull())))
        val errorProcessor = ErrorProcessor()

        val errorInfo = errorProcessor.getDetailedError(httpException)

        assertThat(errorInfo).isInstanceOf(ErrorInfo::class.java)
        assertThat(errorInfo.errorMessage).contains("HTTP 404")
        assertThat(errorInfo.errorMessage).contains("Not Found")
        assertThat(errorInfo.githubError?.message).isEqualTo("Not Found")
        assertThat(errorInfo.githubError?.status).isEqualTo(404)
    }

    @Test
    fun `getDetailedError - given HttpException with non-JSON error body - returns ErrorInfo processed data`() {
        val nonJsonErrorBody = "Some plain text error"
        val httpException = HttpException(Response.error<Any>(500, nonJsonErrorBody.toResponseBody("text/plain".toMediaTypeOrNull())))
        val errorProcessor = ErrorProcessor()

        val errorInfo = errorProcessor.getDetailedError(httpException)

        assertThat(errorInfo).isInstanceOf(ErrorInfo::class.java)
        assertThat(errorInfo.errorMessage).contains("HTTP 500")
        assertThat(errorInfo.errorMessage).contains("Some plain text error")
        assertThat(errorInfo.githubError).isNull()
    }

    @Test
    fun `getDetailedError - given HttpException with empty error body - returns ErrorInfo processed data`() {
        val httpException = HttpException(Response.error<Any>(404, "".toResponseBody("text/plain".toMediaTypeOrNull())))
        val errorProcessor = ErrorProcessor()

        val errorInfo = errorProcessor.getDetailedError(httpException)

        assertThat(errorInfo).isInstanceOf(ErrorInfo::class.java)
        assertThat(errorInfo.errorMessage).contains("HTTP 404")
        assertThat(errorInfo.githubError).isNull()
    }

    @Test
    fun `getDetailedError - given HttpException with JSON github errors list - returns ErrorInfo processed data with errors`() {
        // language=JSON
        val jsonErrorBody =
            """
            {
              "message": "Validation Failed",
              "errors": [
                {
                  "message": "The listed users cannot be searched either because the users do not exist or you do not have permission to view the users.",
                  "resource": "Search",
                  "field": "q",
                  "code": "invalid"
                }
              ],
              "documentation_url": "https://docs.github.com/v3/search/",
              "status": "422"
            }
            """.trimIndent()
        val httpException = HttpException(Response.error<Any>(422, jsonErrorBody.toResponseBody("application/json".toMediaTypeOrNull())))
        val errorProcessor = ErrorProcessor()

        val errorInfo = errorProcessor.getDetailedError(httpException)

        assertThat(errorInfo).isInstanceOf(ErrorInfo::class.java)
        assertThat(errorInfo.githubError?.message).isEqualTo("Validation Failed")
        assertThat(errorInfo.githubError?.status).isEqualTo(422)
        assertThat(errorInfo.githubError?.errors).isNotEmpty()

        val githubErrorDetail = errorInfo.githubError?.errors?.get(0)!!
        assertThat(
            githubErrorDetail.message,
        ).isEqualTo(
            "The listed users cannot be searched either because the users do not exist or you do not have permission to view the users.",
        )
        assertThat(githubErrorDetail.resource).isEqualTo("Search")
        assertThat(githubErrorDetail.field).isEqualTo("q")
        assertThat(githubErrorDetail.code).isEqualTo("invalid")
    }

    @Test
    fun `getDetailedError - given HttpException with JSON github error user not found - validates user not found`() {
        // language=JSON
        val jsonErrorBody =
            """
            {
              "message": "Validation Failed",
              "errors": [
                {
                  "message": "The listed users cannot be searched either because the users do not exist or you do not have permission to view the users.",
                  "resource": "Search",
                  "field": "q",
                  "code": "invalid"
                }
              ],
              "documentation_url": "https://docs.github.com/v3/search/",
              "status": "422"
            }
            """.trimIndent()
        val httpException = HttpException(Response.error<Any>(422, jsonErrorBody.toResponseBody("application/json".toMediaTypeOrNull())))
        val errorProcessor = ErrorProcessor()

        val errorInfo = errorProcessor.getDetailedError(httpException)

        assertThat(errorInfo).isInstanceOf(ErrorInfo::class.java)
        assertThat(ErrorProcessor.isUserMissingError(errorInfo.githubError)).isTrue()
    }

    @Test
    fun `getDetailedError - given HttpException with rate limit error - validates rate limit error`() {
        // language=JSON
        val jsonErrorBody =
            """
            {
              "message": "API rate limit exceeded for user ID 99822. If you reach out to GitHub Support for help, please include the request ID CF05:4DFD1:19AA6E5:33F8C4B:687A2E8E and timestamp 2025-07-18 11:22:54 UTC.",
              "documentation_url": "https://docs.github.com/rest/overview/rate-limits-for-the-rest-api",
              "status": "403"
            }
            """.trimIndent()
        val httpException = HttpException(Response.error<Any>(403, jsonErrorBody.toResponseBody("application/json".toMediaTypeOrNull())))
        val errorProcessor = ErrorProcessor()

        val errorInfo = errorProcessor.getDetailedError(httpException)

        assertThat(errorInfo).isInstanceOf(ErrorInfo::class.java)
        assertThat(errorInfo.errorMessage).contains("HTTP 403")
        assertThat(errorInfo.errorMessage).contains("API rate limit exceeded")
        assertThat(errorInfo.githubError?.message).contains("API rate limit exceeded")
        assertThat(errorInfo.githubError?.status).isEqualTo(403)
        assertThat(ErrorProcessor.isRateLimitError(errorInfo.githubError)).isTrue()
        assertThat(errorInfo.isRateLimitError()).isTrue()
    }
}
