package dev.hossain.githubstats.formatter

import dev.hossain.githubstats.PullStats

/**
 * Formatter/decorator for different stats.
 */
interface StatsFormatter {
    fun formatPrStats(prStats: PullStats.StatsResult.Success): String
}
