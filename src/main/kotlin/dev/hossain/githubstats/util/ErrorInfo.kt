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
 * Error threshold information.
 */
data class ErrorThreshold(
    val exceeded: Boolean,
    val errorMessage: String,
)

/**
 * Error response from GitHub API.
 *
 * Sample error messages.
 * ```json
 * {"message":"Bad credentials","documentation_url":"https://docs.github.com/rest"}
 *
 * {"message":"Validation Failed","errors":[{"resource":"Issue","code":"missing_field","field":"title"}],"documentation_url":"https://docs.github.com/rest/reference/issues#create-an-issue"}
 *
 * {"message":"Not Found","documentation_url":"https://docs.github.com/rest/pulls/pulls#get-a-pull-request","status":"404"}
 *
 * {"message":"Validation Failed","errors":[{"message":"The listed users cannot be searched either because the users do not exist or you do not have permission to view the users.","resource":"Search","field":"q","code":"invalid"}],"documentation_url":"https://docs.github.com/v3/search/","status":"422"}
 * ```
 */
@JsonClass(generateAdapter = true)
data class GithubError(
    @Json(name = "message") val message: String,
    @Json(name = "documentation_url") val documentationUrl: String,
    @Json(name = "status") val status: Int? = null,
    @Json(name = "errors") val errors: List<GithubErrorDetail> = emptyList(),
)

/**
 * Error details from GitHub API.
 *
 * Example error detail:
 * ```json
 * {
 *   "resource": "Search",
 *   "code": "invalid",
 *   "message": "The listed users cannot be searched either because the users do not exist or you do not have permission to view the users.",
 *   "field": "q"
 * }
 * ```
 */
@JsonClass(generateAdapter = true)
data class GithubErrorDetail(
    @Json(name = "resource") val resource: String,
    @Json(name = "code") val code: String,
    @Json(name = "field") val field: String,
    @Json(name = "message") val message: String,
)
