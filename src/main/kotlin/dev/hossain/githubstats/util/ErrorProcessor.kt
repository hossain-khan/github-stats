package dev.hossain.githubstats.util

import dev.hossain.githubstats.AppConstants.BUILD_CONFIG
import dev.hossain.githubstats.AppConstants.GITHUB_TOKEN_SETTINGS_URL
import dev.hossain.githubstats.AppConstants.LOCAL_PROPERTIES_FILE
import dev.hossain.githubstats.BuildConfig
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Response

class ErrorProcessor {
    companion object {
        /**
         * Error message when token is invalid.
         *
         * Sample error message.
         * ```json
         * {"message":"Bad credentials","documentation_url":"https://docs.github.com/rest"}
         * ```
         */
        private const val TOKEN_ERROR_MESSAGE = "Bad credentials"
    }

    /**
     * Provides exception with detailed message to debug the error.
     */
    fun getDetailedError(exception: Exception): Exception {
        return IllegalStateException(getErrorMessage(exception), exception)
    }

    /**
     * Provides bit more verbose error message to help understand the error.
     */
    private fun getErrorMessage(exception: Exception): String {
        // Tell about HTTP Response Headers has important debug information
        // which might help user to debug failing request
        val debugGuide = if (BuildConfig.DEBUG_HTTP_REQUESTS) "" else httpResponseDebugGuide

        if (exception is HttpException) {
            val response: Response<*>? = exception.response()
            val error: ResponseBody? = response?.errorBody()
            val message: String = exception.message ?: "HTTP Error ${exception.code()}"

            if (error != null) {
                val errorContent = error.string()
                return "$message - ${errorContent}${getTokenErrorGuide(errorContent)}\n$debugGuide"
            }
            return "${message}\n$debugGuide"
        } else {
            return "${exception.message}\n$debugGuide"
        }
    }

    private fun getTokenErrorGuide(errorMessage: String): String {
        println("Error message: $errorMessage")
        return if (errorMessage.contains(TOKEN_ERROR_MESSAGE)) {
            """
            
            
            ------------------------------------------------------------------------------------------------
            ⚠️ NOTE: Your token likely have expired. 
                     You can create a new token from GitHub settings page and provide it in `[$LOCAL_PROPERTIES_FILE]`.
                     See: $GITHUB_TOKEN_SETTINGS_URL
            ------------------------------------------------------------------------------------------------
            """.trimIndent()
        } else {
            ""
        }
    }

    private val httpResponseDebugGuide: String =
        """
        
        ------------------------------------------------------------------------------------------------
        NOTE: You can turn on HTTP request and response debugging that contains
              HTTP response header containing important information like API rate limit.
        
        You can turn on this feature by opening `[$BUILD_CONFIG]` and setting `DEBUG_HTTP_REQUESTS = true`.
        ------------------------------------------------------------------------------------------------
        
        """.trimIndent()
}
