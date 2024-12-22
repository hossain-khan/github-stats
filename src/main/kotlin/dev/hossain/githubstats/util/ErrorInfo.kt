package dev.hossain.githubstats.util

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Error information that can be used to provide user-friendly error message.
 */
data class ErrorInfo(
    val errorMessage: String,
    val exception: Exception,
    val debugGuideMessage: String = "",
    val githubError: GithubError? = null,
)

/**
 * Error response from GitHub API.
 *
 * Sample error message.
 * ```json
 * {"message":"Bad credentials","documentation_url":"https://docs.github.com/rest"}
 *
 * {"message":"Validation Failed","errors":[{"resource":"Issue","code":"missing_field","field":"title"}],"documentation_url":"https://docs.github.com/rest/reference/issues#create-an-issue"}
 *
 * {"message":"Not Found","documentation_url":"https://docs.github.com/rest/pulls/pulls#get-a-pull-request","status":"404"}
 * ```
 */
@JsonClass(generateAdapter = true)
data class GithubError(
    @Json(name = "message") val message: String,
    @Json(name = "documentation_url") val documentationUrl: String,
    @Json(name = "status") val status: Int? = null,
)
