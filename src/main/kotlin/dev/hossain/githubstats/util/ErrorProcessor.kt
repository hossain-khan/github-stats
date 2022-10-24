package dev.hossain.githubstats.util

import dev.hossain.githubstats.AppConstants.BUILD_CONFIG
import dev.hossain.githubstats.BuildConfig
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Response

class ErrorProcessor {
    /**
     * Provides exception with detailed message to debug the error.
     */
    fun getDetailedError(exception: Exception): Exception {
        return IllegalStateException(getErrorMessage(exception), exception)
    }

    /**
     * Provides bit more verbose error message to help understand the error.
     */
    fun getErrorMessage(exception: Exception): String {
        // Tell about HTTP Response Headers has important debug information
        // which might help user to debug failing request
        val debugGuide = if (BuildConfig.DEBUG_HTTP_REQUESTS) "" else httpResponseDebugGuide

        if (exception is HttpException) {
            val response: Response<*>? = exception.response()
            val error: ResponseBody? = response?.errorBody()
            val message: String = exception.message ?: "HTTP Error ${exception.code()}"

            if (error != null) {
                return "$message - ${error.string()}\n$debugGuide"
            }
            return "${message}\n$debugGuide"
        } else {
            return "${exception.message}\n$debugGuide"
        }
    }

    private val httpResponseDebugGuide: String = """
        ------------------------------------------------------------------------------------------------
        NOTE: You can turn on HTTP request and response debugging that contains
              HTTP response header containing important information like API rate limit.
        
        You can turn on this feature by opening `[$BUILD_CONFIG]` and setting `DEBUG_HTTP_REQUESTS = true`.
        ------------------------------------------------------------------------------------------------
    """.trimIndent()
}
