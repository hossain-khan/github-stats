package dev.hossain.githubstats

import dev.hossain.githubstats.UserPrComment.Companion.noComments
import dev.hossain.githubstats.logging.Log
import dev.hossain.githubstats.model.Issue
import dev.hossain.githubstats.repository.PullRequestStatsRepo
import dev.hossain.githubstats.repository.PullRequestStatsRepo.StatsResult
import dev.hossain.githubstats.service.IssueSearchPagerService
import dev.hossain.githubstats.service.SearchParams
import dev.hossain.githubstats.util.AppConfig
import dev.hossain.githubstats.util.ErrorProcessor
import dev.hossain.githubstats.util.PrAnalysisProgress
import kotlinx.coroutines.delay
import kotlin.time.Duration

/**
 * PR review stats for all the PRs reviewed by specific reviewer.
 *
 * @see PrAuthorStatsService
 */
class PrReviewerStatsService constructor(
    private val pullRequestStatsRepo: PullRequestStatsRepo,
    private val issueSearchPager: IssueSearchPagerService,
    private val appConfig: AppConfig,
    private val errorProcessor: ErrorProcessor,
) {
    suspend fun reviewerStats(prReviewerUserId: String): ReviewerReviewStats {
        val (repoOwner, repoId, _, botUserIds, dateLimitAfter, dateLimitBefore) = appConfig.get()

        // First get all the recent PRs reviewed by the user
        val reviewedClosedPrs: List<Issue> =
            issueSearchPager
                .searchIssues(
                    searchQuery =
                        SearchParams(
                            repoOwner = repoOwner,
                            repoId = repoId,
                            reviewer = prReviewerUserId,
                            dateAfter = dateLimitAfter,
                            dateBefore = dateLimitBefore,
                        ).toQuery(),
                ).filter {
                    // Makes sure it is a PR, not an issue
                    it.pull_request != null
                }

        // Provides periodic progress updates based on config
        val progress = PrAnalysisProgress(reviewedClosedPrs).also { it.start() }

        // For each of the PRs reviewed by the reviewer, get the stats
        val prStatsListReviewedByReviewer: List<PrStats> =
            reviewedClosedPrs
                .mapIndexed { index, pr ->
                    progress.publish(index)

                    // ⏰ Slight delay to avoid GitHub API rate-limit
                    delay(BuildConfig.API_REQUEST_DELAY_MS)

                    try {
                        pullRequestStatsRepo.stats(
                            repoOwner = repoOwner,
                            repoId = repoId,
                            prNumber = pr.number,
                            botUserIds = botUserIds,
                        )
                    } catch (e: Exception) {
                        val error = errorProcessor.getDetailedError(e)
                        println("Error getting PR#${pr.number}. Got: ${error.message}")
                        StatsResult.Failure(error)
                    }
                }.filterIsInstance<StatsResult.Success>()
                .map {
                    it.stats
                }

        progress.end()

        // Builds `ReviewStats` list for PRs that are reviewed by specified reviewer user-id
        val reviewerPrReviewStatsList: List<ReviewStats> =
            prStatsListReviewedByReviewer
                .filter {
                    // Ensures that the PR was approved by the reviewer requested in the function
                    it.prApprovalTime.containsKey(prReviewerUserId)
                }.map { stats ->
                    val prApprovalTime = stats.prApprovalTime[prReviewerUserId]!!
                    ReviewStats(
                        reviewerUserId = prReviewerUserId,
                        pullRequest = stats.pullRequest,
                        reviewCompletion = prApprovalTime,
                        initialResponseTime = stats.initialResponseTime[prReviewerUserId] ?: prApprovalTime,
                        prComments = stats.comments[prReviewerUserId] ?: noComments(prReviewerUserId),
                        prReadyOn = stats.prReadyOn,
                        prMergedOn = stats.prMergedOn,
                    )
                }

        // Builds a hashmap for [Reviewed for UserID -> List of PR Reviewed and their Stats]
        // For example:
        //  * john -> [PR#112 Stats, PR#931 Stats] (Meaning: The reviewer has reviewed 2 PRs created by `john`)
        //  * kirk -> [PR#341 Stats, PR#611 Stats, PR#839 Stats]  (Meaning: The reviewer has reviewed 3 PRs created by `kirk`)
        val reviewerReviewedFor = mutableMapOf<UserId, List<PrStats>>()
        prStatsListReviewedByReviewer
            .filter {
                // Ensures that the PR was reviewed by the reviewer requested in the function
                it.prApprovalTime.containsKey(prReviewerUserId)
            }.forEach { prStats ->
                val prAuthorUserId = prStats.pullRequest.user.login
                if (reviewerReviewedFor.containsKey(prAuthorUserId)) {
                    reviewerReviewedFor[prAuthorUserId] = reviewerReviewedFor[prAuthorUserId]!!.plus(prStats)
                } else {
                    reviewerReviewedFor[prAuthorUserId] = listOf(prStats)
                }
            }

        Log.i("✅ Completed loading ${prStatsListReviewedByReviewer.size} PRs reviewed by '$prReviewerUserId'.")

        // Finally build the data object that combines all related stats
        return ReviewerReviewStats(
            repoId = repoId,
            reviewerId = prReviewerUserId,
            average =
                if (reviewerPrReviewStatsList.isEmpty()) {
                    Duration.ZERO
                } else {
                    reviewerPrReviewStatsList
                        .map { it.reviewCompletion }
                        .fold(Duration.ZERO, Duration::plus)
                        .div(reviewerPrReviewStatsList.size)
                },
            totalReviews = reviewerPrReviewStatsList.size,
            reviewedPrStats = reviewerPrReviewStatsList,
            reviewedForPrStats = reviewerReviewedFor,
        )
    }
}
