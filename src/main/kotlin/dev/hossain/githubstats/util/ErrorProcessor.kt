package dev.hossain.githubstats.util

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dev.hossain.githubstats.AppConstants.BUILD_CONFIG
import dev.hossain.githubstats.AppConstants.GITHUB_TOKEN_SETTINGS_URL
import dev.hossain.githubstats.AppConstants.LOCAL_PROPERTIES_FILE
import dev.hossain.githubstats.BuildConfig
import dev.hossain.i18n.Resources
import okhttp3.ResponseBody
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import retrofit2.HttpException
import retrofit2.Response

/**
 * A utility class to process errors and provide detailed error information using [ErrorInfo].
 */
class ErrorProcessor : KoinComponent {
    private val resources: Resources by inject()

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

        /**
         * Error message when search query is invalid.
         *
         * Sample error message.
         * ```json
         * {"message":"Validation Failed","errors":[{"message":"The listed users cannot be searched.","resource":"Search","field":"q","code":"invalid"}],"documentation_url":"https://docs.github.com/v3/search/","status":"422"}
         * ```
         */
        private const val VALIDATION_FAILED_ERROR_MESSAGE = "Validation Failed"

        /**
         * Resource type for search error.
         */
        private const val RESOURCE_TYPE_SEARCH = "Search"

        /**
         * Check if user is missing in the search query.
         */
        fun isUserMissingError(githubError: GithubError?): Boolean {
            if (githubError == null || githubError.message != VALIDATION_FAILED_ERROR_MESSAGE) {
                return false
            }

            return githubError.errors.any {
                it.resource == RESOURCE_TYPE_SEARCH &&
                    // Yes, hardcoding the server message string to avoid any false positive
                    it.message.contains("users cannot be searched")
            }
        }
    }

    /**
     * Provides exception with detailed message to debug the error.
     *
     * @param exception The exception to process.
     * @return An `ErrorInfo` object containing detailed error information.
     */
    fun getDetailedError(exception: Exception): ErrorInfo = getErrorInformation(exception)

    /**
     * Provides bit more verbose error message to help understand the error.
     *
     * @param exception The exception to process.
     * @return An `ErrorInfo` object containing detailed error information.
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
     *
     * @param errorContentJson The JSON string containing the GitHub error response.
     * @return A `GithubError` object if parsing is successful, `null` otherwise.
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

    /**
     * Provides a guide message for token errors.
     *
     * @param errorMessage The error message to check.
     * @return A guide message if the error message contains token error, an empty string otherwise.
     */
    private fun getTokenErrorGuide(errorMessage: String): String {
        println(resources.string("error_message_debug", errorMessage))
        return if (errorMessage.contains(TOKEN_ERROR_MESSAGE)) {
            resources.string("error_token_expired_guide", LOCAL_PROPERTIES_FILE, GITHUB_TOKEN_SETTINGS_URL)
        } else {
            ""
        }
    }

    /**
     * Debug guide message for HTTP response headers.
     */
    private val httpResponseDebugGuide: String
        get() = resources.string("error_http_debug_guide", BUILD_CONFIG)
}
