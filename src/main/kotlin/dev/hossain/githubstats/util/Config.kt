package dev.hossain.githubstats.util

/**
 * Repository and user configs to run the PR stats.
 */
data class Config(
    val repoOwner: String,
    val repoId: String,
    val dateLimit: String,
    val userIds: List<String>
)
