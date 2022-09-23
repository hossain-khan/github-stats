package dev.hossain.githubstats.model

/**
 * A GitHub user with basic information.
 */
data class User(
    val avatar_url: String?,
    val id: Long,
    val login: String,
    val repos_url: String?,
    val type: String?,
    val url: String?
)
