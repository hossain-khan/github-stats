package dev.hossain.githubstats

import dev.hossain.githubstats.model.Issue
import dev.hossain.githubstats.service.IssueSearchPager
import dev.hossain.githubstats.service.SearchParams
import kotlinx.coroutines.delay
import java.time.ZoneId

/**
 * PR review stats for all the PRs reviewed by specific reviewer.
 *
 * @see PrAuthorStats
 */
class PrReviewerStats constructor(
    private val issueSearchPager: IssueSearchPager,
    private val pullStats: PullStats
) {

    suspend fun reviewerStats(
        owner: String,
        repo: String,
        reviewer: String,
        zoneId: ZoneId
    ) {
        val reviewedClosedPrs: List<Issue> = issueSearchPager.searchIssues(
            searchQuery = SearchParams(repoOwner = owner, repoId = repo, reviewer = reviewer).toQuery()
        )

        // For each of the PRs reviewed by the reviewer, get the stats
        val prStatsList: List<PrStats> = reviewedClosedPrs
            .filter {
                // Makes sure it is a PR, not an issue
                it.pull_request != null
            }
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

        // Make 2 kinds of stats i guess?
        // 1. All the PRs reviewed by the reviewer and their time
        // 2. Some stats about how many PRs are reviewed by the reviewer for specific author
        // 3. Also may be who does reviewer enjoy reviewing with?
        val reviewerPrStats: List<ReviewStats> = prStatsList.filter { it.reviewTime.containsKey(reviewer) }
            .map { stats ->
                ReviewStats(
                    pullRequest = stats.pullRequest,
                    reviewCompletion = stats.reviewTime[reviewer]!!,
                    prReadyOn = stats.prReadyOn,
                    prMergedOn = stats.prMergedOn
                )
            }

        val reviewerReviewedFor = mutableMapOf<UserId, List<PrStats>>()
        prStatsList.filter { it.reviewTime.containsKey(reviewer) }.forEach { prStats ->
            val prAuthorUserId = prStats.pullRequest.user.login
            if (reviewerReviewedFor.containsKey(prAuthorUserId)) {
                reviewerReviewedFor[prAuthorUserId] = reviewerReviewedFor[prAuthorUserId]!!.plus(prStats)
            } else {
                reviewerReviewedFor[prAuthorUserId] = listOf(prStats)
            }
        }

        println("Gotta process reviewer's stats for ${prStatsList.size} PRs. ${reviewerPrStats.size}, ${reviewerReviewedFor.size}")

        if (BuildConfig.DEBUG) {
            println("✅ Completed loading ${prStatsList.size} PRs reviewed by '$reviewer'.")
        }
    }
}
