package dev.hossain.githubstats

import dev.hossain.githubstats.BuildConfig.REPO_ID
import dev.hossain.githubstats.BuildConfig.REPO_OWNER
import dev.hossain.githubstats.service.GithubService
import dev.hossain.githubstats.service.SearchParams
import kotlinx.coroutines.delay

class PrAuthorStats constructor(
    private val githubService: GithubService,
    private val pullStats: PullStats
) {

    suspend fun authorStats(author: String): List<PrStats> {
        val closedPrs = githubService.searchIssues(
            searchQuery = SearchParams(repoOwner = REPO_OWNER, repoId = REPO_ID, author = "DanielRosa74").toQuery(),
            page = 1,
            size = 30
        )

        return closedPrs.items
            .filter { it.pull_request != null }
            .map {
                delay(200) // Slight delay to avoid per-second limit

                try {
                    pullStats.calculateStats(it.number)
                } catch (e: Exception) {
                    println("Error getting PR#${it.number}. Got: ${e.message}")
                    PullStats.StatsResult.Failure(e)
                }
            }
            .filterIsInstance<PullStats.StatsResult.Success>()
            .map {
                it.stats
            }
    }
}
