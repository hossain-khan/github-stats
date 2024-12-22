package dev.hossain.githubstats.util

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
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
    fun getDetailedError(exception: Exception): ErrorInfo = getErrorInformation(exception)

    /**
     * Provides bit more verbose error message to help understand the error.
     */
    private fun getErrorInformation(exception: Exception): ErrorInfo {
        // Tell about HTTP Response Headers has important debug information
        // which might help user to debug failing request
        val debugGuide = if (BuildConfig.DEBUG_HTTP_REQUESTS) "" else httpResponseDebugGuide

        if (exception is HttpException) {
            val response: Response<*>? = exception.response()
            val error: ResponseBody? = response?.errorBody()
            val message: String = exception.message ?: "HTTP Error ${exception.code()}"

            if (error != null) {
                val errorContentJson = error.string()
                val githubError = getGithubError(errorContentJson)
                return ErrorInfo(
                    errorMessage = "$message - $errorContentJson",
                    debugGuideMessage = "${getTokenErrorGuide(errorContentJson)}\n$debugGuide",
                    exception = IllegalStateException("$message - $errorContentJson", exception),
                    githubError = githubError,
                )
            }
            return ErrorInfo(
                errorMessage = message,
                debugGuideMessage = debugGuide,
                exception = IllegalStateException(message, exception),
            )
        } else {
            return ErrorInfo(
                errorMessage = exception.message ?: "Unknown error",
                debugGuideMessage = debugGuide,
                exception = exception,
            )
        }
    }

    /**
     * Parse GitHub API error response.
     */
    private fun getGithubError(errorContentJson: String): GithubError? =
        try {
            Moshi
                .Builder()
                .addLast(KotlinJsonAdapterFactory())
                .build()
                .adapter(GithubError::class.java)
                .fromJson(errorContentJson)
        } catch (e: Exception) {
            null
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
