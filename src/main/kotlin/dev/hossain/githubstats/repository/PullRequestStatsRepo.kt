package dev.hossain.githubstats.repository

import dev.hossain.githubstats.PrStats
import java.time.ZoneId

interface PullRequestStatsRepo {
    sealed class StatsResult {
        data class Success(
            val stats: PrStats
        ) : StatsResult()

        data class Failure(
            val error: Throwable
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
    suspend fun stats(repoOwner: String, repoId: String, prNumber: Int, zoneId: ZoneId): StatsResult
}
