package dev.hossain.githubstats.formatter

import dev.hossain.githubstats.AuthorReviewStats
import dev.hossain.githubstats.PrStats

/**
 * Formatter/decorator for different stats.
 */
interface StatsFormatter {
    /**
     * Formats PR review stats for a specific single PR.
     */
    fun formatPrStats(prStats: PrStats): String

    /**
     * Formats PR review stats for list of authors.
     */
    fun formatAuthorStats(stats: List<AuthorReviewStats>): String
}
