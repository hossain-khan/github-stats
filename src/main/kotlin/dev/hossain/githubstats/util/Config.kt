package dev.hossain.githubstats.util

import dev.hossain.githubstats.AppConstants.LOCAL_PROPERTIES_FILE
import dev.hossain.githubstats.AppConstants.LOCAL_PROPERTIES_SAMPLE_FILE

/**
 * Repository and user configs to run the PR stats.
 * These configs are loaded from [LOCAL_PROPERTIES_FILE].
 * @see LOCAL_PROPERTIES_FILE
 * @see LOCAL_PROPERTIES_SAMPLE_FILE
 */
data class Config(
    /**
     * Repository OwnerId or OrgId.
     * For example, if repo URL is https://github.com/Foso/Ktorfit
     * The [repoOwner] would be `Foso` and [repoId] would be `Ktorfit`
     */
    val repoOwner: String,
    /**
     * Unique repository name that is used in URL.
     */
    val repoId: String,
    /**
     * List of authors & reviewers user-id to generate report for
     * Use `,` comma separated list of user-ids. For example: user1,user2
     */
    val userIds: List<String>,
    /**
     * Limits the search at MIN given date to exclude older data
     * (Format: YYYY-MM-DD)
     */
    val dateLimitAfter: String,
    /**
     * (Optional) Limits the search at MAX given date to exclude newer data
     * (Format: YYYY-MM-DD)
     */
    val dateLimitBefore: String
)
