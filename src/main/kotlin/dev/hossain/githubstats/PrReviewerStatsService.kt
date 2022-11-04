package dev.hossain.githubstats

import dev.hossain.githubstats.UserPrComment.Companion.empty
import dev.hossain.githubstats.model.Issue
import dev.hossain.githubstats.repository.PullRequestStatsRepo
import dev.hossain.githubstats.repository.PullRequestStatsRepo.StatsResult
import dev.hossain.githubstats.service.IssueSearchPager
import dev.hossain.githubstats.service.SearchParams
import dev.hossain.githubstats.util.AppConfig
import dev.hossain.githubstats.util.ErrorProcessor
import dev.hossain.githubstats.util.PrAnalysisProgress
import kotlinx.coroutines.delay
import org.koin.core.component.KoinComponent
import kotlin.time.Duration

/**
 * PR review stats for all the PRs reviewed by specific reviewer.
 *
 * @see PrAuthorStatsService
 */
class PrReviewerStatsService constructor(
    private val pullRequestStatsRepo: PullRequestStatsRepo,
    private val appConfig: AppConfig,
    private val errorProcessor: ErrorProcessor
) : KoinComponent {
    suspend fun reviewerStats(
        prReviewerUserId: String
    ): ReviewerReviewStats {
        val (repoOwner, repoId, _, dateLimitAfter, dateLimitBefore) = appConfig.get()

        val issueSearchPager: IssueSearchPager = getKoin().get()
        val reviewedClosedPrs: List<Issue> = issueSearchPager.searchIssues(
            searchQuery = SearchParams(
                repoOwner = repoOwner,
                repoId = repoId,
                reviewer = prReviewerUserId,
                dateAfter = dateLimitAfter,
                dateBefore = dateLimitBefore
            ).toQuery()
        ).filter {
            // Makes sure it is a PR, not an issue
            it.pull_request != null
        }

        // Provides periodic progress updates based on config
        val progress = PrAnalysisProgress(reviewedClosedPrs).also { it.start() }

        // For each of the PRs reviewed by the reviewer, get the stats
        val prStatsList: List<PrStats> = reviewedClosedPrs
            .mapIndexed { index, pr ->
                progress.publish(index)
                delay(BuildConfig.API_REQUEST_DELAY_MS) // Slight delay to avoid per-second limit

                try {
                    pullRequestStatsRepo.stats(
                        repoOwner = repoOwner,
                        repoId = repoId,
                        prNumber = pr.number
                    )
                } catch (e: Exception) {
                    val error = errorProcessor.getDetailedError(e)
                    println("Error getting PR#${pr.number}. Got: ${error.message}")
                    StatsResult.Failure(error)
                }
            }
            .filterIsInstance<StatsResult.Success>()
            .map {
                it.stats
            }

        progress.end()

        // Builds `ReviewStats` list for PRs that are reviewed by specified reviewer user-id
        val reviewerPrStats: List<ReviewStats> = prStatsList
            .filter {
                // Ensures that the PR was reviewed by the reviewer requested in the function
                it.reviewTime.containsKey(prReviewerUserId)
            }
            .map { stats ->
                ReviewStats(
                    pullRequest = stats.pullRequest,
                    reviewCompletion = stats.reviewTime[prReviewerUserId]!!,
                    prComments = stats.comments[prReviewerUserId] ?: empty(prReviewerUserId),
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
                it.reviewTime.containsKey(prReviewerUserId)
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
            println("✅ Completed loading ${prStatsList.size} PRs reviewed by '$prReviewerUserId'.")
        }

        // Finally build the data object that combines all related stats
        return ReviewerReviewStats(
            repoId = repoId,
            reviewerId = prReviewerUserId,
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
