package dev.hossain.githubstats

import dev.hossain.githubstats.model.Issue
import dev.hossain.githubstats.service.IssueSearchPager
import dev.hossain.githubstats.service.ReviewerSearchParams
import kotlinx.coroutines.delay
import java.time.ZoneId
import kotlin.time.Duration

/**
 * Generates PR stats for all the PRs created by the specific author.
 *
 * @see PrReviewerStats
 */
class PrAuthorStats constructor(
    private val issueSearchPager: IssueSearchPager,
    private val pullStats: PullStats
) {

    /**
     * Generates stats for reviews given by different PR reviewers for specified PR [author].
     *
     * For example, assume 'Bob' is a contributor on a specific repo called 'Awesome Json Library'.
     * This will be generated PR reviews for all the PRs 'Bob' has created and will be grouped by
     * all the PR authors like 'Sally', 'Mike', 'Jim' and so on.
     *
     * ```
     * Sally -> 78 PRs reviewed; Average Review Time: 2 hours 8 min
     * Mike -> 42 PRs reviewed; Average Review Time: 8 hours 32 min
     * Jim -> 13 PRs reviewed; Average Review Time: 14 hours 21 min
     * ```
     */
    suspend fun authorStats(
        owner: String,
        repo: String,
        author: String,
        zoneId: ZoneId
    ): List<AuthorReviewStats> {
        // First get all the recent PRs made by author
        val closedPrs: List<Issue> = issueSearchPager.searchIssues(
            // searchQuery = SearchParams(repoOwner = owner, repoId = repo, author = author).toQuery()
            searchQuery = ReviewerSearchParams(repoOwner = owner, repoId = repo, reviewer = author).toQuery()
        )

        // For each PR by author, get the review stats on the PR
        val prStatsList: List<PrStats> = closedPrs
            .filter { it.pull_request != null }
            .map {
                delay(BuildConfig.API_REQUEST_DELAY_MS) // Slight delay to avoid per-second limit

                try {
                    pullStats.calculateStats(
                        owner = owner,
                        repo = repo,
                        prNumber = it.number,
                        zoneId = zoneId
                    )
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

        if (BuildConfig.DEBUG) {
            println("✅ Completed loading PR review stats from ${authorReviewStats.size} reviewers.")
        }

        return authorReviewStats
    }
}
