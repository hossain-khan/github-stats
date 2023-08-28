package dev.hossain.githubstats.formatter

import dev.hossain.githubstats.AuthorPrStats
import dev.hossain.githubstats.AuthorStats
import dev.hossain.githubstats.PrStats
import dev.hossain.githubstats.ReviewerReviewStats

/**
 * Formatter/decorator for different stats.
 */
interface StatsFormatter {
    /**
     * Formats PR review stats for a specific single PR.
     *
     * @return Provide formatted output or useful information after formatting completion like file path.
     */
    fun formatSinglePrStats(prStats: PrStats): String

    /**
     * Formats PR review stats for list of users that reviewed specific user's PRs.
     *
     * @return Provide formatted output or useful information after formatting completion like file path.
     */
    fun formatAuthorStats(stats: AuthorStats): String

    /**
     * This formats all authors stats to provided aggregated information about authors.
     *
     * @return Provide formatted output or useful information after formatting completion like file path.
     */
    fun formatAllAuthorStats(aggregatedPrStats: List<AuthorPrStats>): String

    /**
     * Formats [ReviewerReviewStats] that contains all review stats given by the reviewer.
     *
     * @return Provide formatted output or useful information after formatting completion like file path.
     */
    fun formatReviewerStats(stats: ReviewerReviewStats): String
}
