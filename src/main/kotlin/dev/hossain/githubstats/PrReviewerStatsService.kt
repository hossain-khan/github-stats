package dev.hossain.githubstats

import dev.hossain.githubstats.model.Issue
import dev.hossain.githubstats.repository.PullRequestStatsRepo
import dev.hossain.githubstats.repository.PullRequestStatsRepo.StatsResult
import dev.hossain.githubstats.service.IssueSearchPager
import dev.hossain.githubstats.service.SearchParams
import kotlinx.coroutines.delay
import org.koin.core.component.KoinComponent
import java.time.ZoneId
import kotlin.time.Duration

/**
 * PR review stats for all the PRs reviewed by specific reviewer.
 *
 * @see PrAuthorStatsService
 */
class PrReviewerStatsService constructor(
    private val pullRequestStatsRepo: PullRequestStatsRepo
) : KoinComponent {
    suspend fun reviewerStats(
        owner: String,
        repo: String,
        reviewerUserId: String,
        zoneId: ZoneId,
        dateLimit: String
    ): ReviewerReviewStats {
        val issueSearchPager: IssueSearchPager = getKoin().get()
        val reviewedClosedPrs: List<Issue> = issueSearchPager.searchIssues(
            searchQuery = SearchParams(repoOwner = owner, repoId = repo, reviewer = reviewerUserId, dateAfter = dateLimit).toQuery()
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
                        repoOwner = owner,
                        repoId = repo,
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

        // Builds `ReviewStats` list for PRs that are reviewed by specified reviewer user-id
        val reviewerPrStats: List<ReviewStats> = prStatsList
            .filter {
                // Ensures that the PR was reviewed by the reviewer requested in the function
                it.reviewTime.containsKey(reviewerUserId)
            }
            .map { stats ->
                ReviewStats(
                    pullRequest = stats.pullRequest,
                    reviewCompletion = stats.reviewTime[reviewerUserId]!!,
                    prReadyOn = stats.prReadyOn,
                    prMergedOn = stats.prMergedOn
                )
            }

        // Builds a hashmap for [Reviewed for UserID -> List of PR Reviewed and their Stats]
        // For example:
        //  * john -> [PR#112 Stats, PR#931 Stats] (Meaning: The reviewer has reviewed 2 PRs created by `john`)
        //  * kirk -> [PR#341 Stats, PR#611 Stats, PR#839 Stats]  (Meaning: The reviewer has reviewed 3 PRs created by `kirk`)
        val reviewerReviewedFor = mutableMapOf<UserId, List<PrStats>>()
        prStatsList
            .filter {
                // Ensures that the PR was reviewed by the reviewer requested in the function
                it.reviewTime.containsKey(reviewerUserId)
            }
            .forEach { prStats ->
                val prAuthorUserId = prStats.pullRequest.user.login
                if (reviewerReviewedFor.containsKey(prAuthorUserId)) {
                    reviewerReviewedFor[prAuthorUserId] = reviewerReviewedFor[prAuthorUserId]!!.plus(prStats)
                } else {
                    reviewerReviewedFor[prAuthorUserId] = listOf(prStats)
                }
            }

        if (BuildConfig.DEBUG) {
            println("✅ Completed loading ${prStatsList.size} PRs reviewed by '$reviewerUserId'.")
        }

        // Finally build the data object that combines all related stats
        return ReviewerReviewStats(
            repoId = repo,
            reviewerId = reviewerUserId,
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
