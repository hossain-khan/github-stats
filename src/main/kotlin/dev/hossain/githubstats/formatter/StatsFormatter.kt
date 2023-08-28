package dev.hossain.githubstats.formatter

import dev.hossain.githubstats.AuthorStats
import dev.hossain.githubstats.PrStats
import dev.hossain.githubstats.ReviewerReviewStats

/**
 * Formatter/decorator for different stats.
 */
interface StatsFormatter {
    /**
     * Formats PR review stats for a specific single PR.
     */
    fun formatSinglePrStats(prStats: PrStats): String

    /**
     * Formats PR review stats for list of users that reviewed specific user's PRs.
     */
    fun formatAuthorStats(stats: AuthorStats): String

    /**
     * This formats all authors stats to provided aggregated information about authors.
     */
    fun formatAllAuthorStats(allAuthors: List<AuthorStats>)

    /**
     * Formats [ReviewerReviewStats] that contains all review stats given by the reviewer.
     */
    fun formatReviewerStats(stats: ReviewerReviewStats): String
}
