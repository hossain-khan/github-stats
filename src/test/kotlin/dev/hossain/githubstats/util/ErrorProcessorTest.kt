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
    fun `getDetailedError - given HttpException with error body - returns IllegalStateException with detailed message`() {
        val httpException = HttpException(Response.error<Any>(404, "Not found".toResponseBody("text/plain".toMediaTypeOrNull())))
        val errorProcessor = ErrorProcessor()

        val errorInfo = errorProcessor.getDetailedError(httpException)

        assertThat(errorInfo).isInstanceOf(ErrorInfo::class.java)
        assertThat(errorInfo.errorMessage).contains("HTTP 404")
        assertThat(errorInfo.errorMessage).contains("Not found")
    }

    @Test
    fun `getDetailedError - given HttpException without error body - returns IllegalStateException with detailed message`() {
        val httpException = HttpException(Response.error<Any>(404, "".toResponseBody("text/plain".toMediaTypeOrNull())))
        val errorProcessor = ErrorProcessor()

        val errorInfo = errorProcessor.getDetailedError(httpException)

        assertThat(errorInfo).isInstanceOf(ErrorInfo::class.java)
        assertThat(errorInfo.errorMessage).contains("HTTP 404")
    }

    @Test
    fun `getDetailedError - given non HttpException - returns IllegalStateException with detailed message`() {
        val exception = Exception("Some error")
        val errorProcessor = ErrorProcessor()

        val errorInfo = errorProcessor.getDetailedError(exception)

        assertThat(errorInfo).isInstanceOf(ErrorInfo::class.java)
        assertThat(errorInfo.errorMessage).contains("Some error")
    }
}
