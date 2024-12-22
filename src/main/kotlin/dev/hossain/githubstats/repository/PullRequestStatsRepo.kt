package dev.hossain.githubstats.repository

import dev.hossain.githubstats.PrStats
import dev.hossain.githubstats.util.ErrorInfo

/**
 * Repository for generating and providing GitHub PR stats.
 * @see PullRequestStatsRepoImpl
 */
interface PullRequestStatsRepo {
    /**
     * Stats generation result used for [stats] request.
     */
    sealed class StatsResult {
        data class Success(
            val stats: PrStats,
        ) : StatsResult()

        data class Failure(
            val errorInfo: ErrorInfo,
        ) : StatsResult()
    }

    /**
     * Provides Pull Request stats [PrStats] for given [prNumber].
     *
     * Example usage:
     * ```kotlin
     * when (val result = pullStats.calculateStats(47550)) {
     *     is PullStats.StatsResult.Failure -> {
     *         println("Got error for stats: ${result.error}")
     *     }
     *     is PullStats.StatsResult.Success -> {
     *         println("Got PR stats: ${result.stats}")
     *     }
     * }
     * ```
     */
    suspend fun stats(
        repoOwner: String,
        repoId: String,
        prNumber: Int,
        botUserIds: List<String>,
    ): StatsResult
}
