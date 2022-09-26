package dev.hossain.githubstats

import dev.hossain.githubstats.service.GithubService
import dev.hossain.githubstats.service.SearchParams
import kotlinx.coroutines.delay
import kotlin.time.Duration

/**
 * Type alias for GitHub user login/id.
 */
typealias UserId = String
class PrAuthorStats constructor(
    private val githubService: GithubService,
    private val pullStats: PullStats
) {

    suspend fun authorStats(owner: String, repo: String, author: String): List<AuthorReviewStats> {
        // First get all the recent PRs made by author
        val closedPrs = githubService.searchIssues(
            searchQuery = SearchParams(repoOwner = owner, repoId = repo, author = author).toQuery()
        )

        // For each PR by author, get the review stats on the PR
        val prStatsList: List<PrStats> = closedPrs.items
            .filter { it.pull_request != null }
            .map {
                delay(100) // Slight delay to avoid per-second limit

                try {
                    pullStats.calculateStats(owner = owner, repo = repo, prNumber = it.number)
                } catch (e: Exception) {
                    println("Error getting PR#${it.number}. Got: ${e.message}")
                    PullStats.StatsResult.Failure(e)
                }
            }
            .filterIsInstance<PullStats.StatsResult.Success>()
            .map {
                it.stats
            }

        val userReviews = mutableMapOf<UserId, List<ReviewStats>>()
        prStatsList.filter { it.reviewTime.isNotEmpty() }
            .forEach { stats: PrStats ->
                stats.reviewTime.forEach { (user, time) ->
                    val reviewStats = ReviewStats(
                        pullRequest = stats.pullRequest,
                        reviewCompletion = time,
                        prReadyOn = stats.prReadyOn,
                        prMergedOn = stats.prMergedOn
                    )
                    if (userReviews.containsKey(user)) {
                        userReviews[user] = userReviews[user]!!.plus(reviewStats)
                    } else {
                        userReviews[user] = listOf(reviewStats)
                    }
                }
            }

        val authorReviewStats: List<AuthorReviewStats> = userReviews.map { (user, authorStats) ->
            val totalReviews: Int = authorStats.size
            val averageReviewTime: Duration = authorStats
                .map { it.reviewCompletion }
                .fold(Duration.ZERO, Duration::plus)
                .div(totalReviews)

            AuthorReviewStats(
                repoId = repo,
                prAuthorId = author,
                reviewerId = user,
                average = averageReviewTime,
                totalReviews = totalReviews,
                stats = authorStats
            )
        }.sortedByDescending { it.totalReviews }

        return authorReviewStats
    }
}
