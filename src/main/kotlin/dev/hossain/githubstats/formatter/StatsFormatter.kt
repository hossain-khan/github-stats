package dev.hossain.githubstats.formatter

import dev.hossain.githubstats.AuthorReviewStats
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
     * Formats PR review stats for list of authors.
     */
    fun formatAuthorStats(stats: List<AuthorReviewStats>): String

    /**
     * Formats [ReviewerReviewStats] that contains all review stats given by the reviewer.
     */
    fun formatReviewerStats(stats: ReviewerReviewStats): String
}
