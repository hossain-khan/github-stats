package dev.hossain.githubstats

import dev.hossain.githubstats.model.Issue
import dev.hossain.githubstats.repository.PullRequestStatsRepo
import dev.hossain.githubstats.repository.PullRequestStatsRepo.StatsResult
import dev.hossain.githubstats.service.IssueSearchPager
import dev.hossain.githubstats.service.SearchParams
import kotlinx.coroutines.delay
import java.time.ZoneId
import kotlin.time.Duration

/**
 * PR review stats for all the PRs reviewed by specific reviewer.
 *
 * @see PrAuthorStatsService
 */
class PrReviewerStatsService constructor(
    private val issueSearchPager: IssueSearchPager,
    private val pullRequestStatsRepo: PullRequestStatsRepo
) {
    suspend fun reviewerStats(
        owner: String,
        repo: String,
        reviewer: String,
        zoneId: ZoneId,
        dateLimit: String
    ): ReviewerReviewStats {
        val reviewedClosedPrs: List<Issue> = issueSearchPager.searchIssues(
            searchQuery = SearchParams(repoOwner = owner, repoId = repo, reviewer = reviewer, dateAfter = dateLimit).toQuery()
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
                    pullRequestStatsRepo.stats(
                        owner = owner,
                        repo = repo,
                        prNumber = it.number,
                        zoneId = zoneId
                    )
                } catch (e: Exception) {
                    println("Error getting PR#${it.number}. Got: ${e.message}")
                    StatsResult.Failure(e)
                }
            }
            .filterIsInstance<StatsResult.Success>()
            .map {
                it.stats
            }

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

        if (BuildConfig.DEBUG) {
            println("✅ Completed loading ${prStatsList.size} PRs reviewed by '$reviewer'.")
        }

        return ReviewerReviewStats(
            repoId = repo,
            reviewerId = reviewer,
            average = if (reviewerPrStats.isEmpty()) {
                Duration.ZERO
            } else {
                reviewerPrStats.map { it.reviewCompletion }
                    .fold(Duration.ZERO, Duration::plus)
                    .div(reviewerPrStats.size)
            },
            totalReviews = reviewerPrStats.size,
            reviewedPrStats = reviewerPrStats,
            reviewedForPrStats = reviewerReviewedFor
        )
    }
}
