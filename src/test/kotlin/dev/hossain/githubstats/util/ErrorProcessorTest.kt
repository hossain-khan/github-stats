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
}
