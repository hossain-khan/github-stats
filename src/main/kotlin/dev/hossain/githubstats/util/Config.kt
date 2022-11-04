package dev.hossain.githubstats.util

import dev.hossain.githubstats.AppConstants.LOCAL_PROPERTIES_FILE

/**
 * Repository and user configs to run the PR stats.
 * @see LOCAL_PROPERTIES_FILE
 */
data class Config(
    val repoOwner: String,
    val repoId: String,
    val userIds: List<String>,
    val dateLimitAfter: String,
    val dateLimitBefore: String
)
