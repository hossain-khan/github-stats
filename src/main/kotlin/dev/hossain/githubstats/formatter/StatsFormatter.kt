package dev.hossain.githubstats.formatter

import dev.hossain.githubstats.PrStats

/**
 * Formatter/decorator for different stats.
 */
interface StatsFormatter {
    fun formatPrStats(prStats: PrStats): String
}
