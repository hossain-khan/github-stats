package dev.hossain.githubstats.model

/**
 * A GitHub user with basic information.
 * See [User API](https://docs.github.com/en/rest/users/users#get-a-user) for more info.
 */
data class User(
    val avatar_url: String?,
    val id: Long,
    val login: String,
    val repos_url: String?,
    val type: String?,
    val url: String?
)
