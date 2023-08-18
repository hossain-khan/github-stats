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
 * Generates PR stats for all the PRs created by the specific author.
 *
 * @see PrReviewerStatsService
 */
class PrAuthorStatsService constructor(
    private val pullRequestStatsRepo: PullRequestStatsRepo,
    private val issueSearchPager: IssueSearchPagerService,
    private val appConfig: AppConfig,
    private val errorProcessor: ErrorProcessor
) {

    /**
     * Generates stats for reviews given by different PR reviewers for specified PR [prAuthorUsedId].
     *
     * For example, assume 'Bob' is a contributor on a specific repo called 'Awesome Json Library'.
     * This will be generated PR reviews for all the PRs 'Bob' has created and will be grouped by
     * all the PR reviewers like 'Sally', 'Mike', 'Jim' and so on.
     *
     * ```
     * Sally -> 78 PRs reviewed for Bob; Average Review Time: 2 hours 8 min
     * Mike -> 42 PRs reviewed for Bob; Average Review Time: 8 hours 32 min
     * Jim -> 13 PRs reviewed for Bob; Average Review Time: 14 hours 21 min
     * ```
     */
    suspend fun authorStats(
        prAuthorUsedId: String
    ): List<AuthorReviewStats> {
        val (repoOwner, repoId, _, dateLimitAfter, dateLimitBefore) = appConfig.get()

        // First get all the recent PRs made by author
        val allMergedPrsByAuthor: List<Issue> = issueSearchPager.searchIssues(
            searchQuery = SearchParams(
                repoOwner = repoOwner,
                repoId = repoId,
                author = prAuthorUsedId,
                dateAfter = dateLimitAfter,
                dateBefore = dateLimitBefore
            ).toQuery()
        ).filter {
            // Makes sure it is a PR, not an issue
            it.pull_request != null
        }

        // Provides periodic progress updates based on config
        val progress = PrAnalysisProgress(allMergedPrsByAuthor).also { it.start() }

        // For each PR by author, get the review stats on the PR
        val mergedPrsStatsList: List<PrStats> = allMergedPrsByAuthor
            .mapIndexed { index, pr ->
                progress.publish(index)

                // ⏰ Slight delay to avoid GitHub API rate-limit
                delay(BuildConfig.API_REQUEST_DELAY_MS)

                try {
                    pullRequestStatsRepo.stats(
                        repoOwner = repoOwner,
                        repoId = repoId,
                        prNumber = pr.number
                    )
                } catch (e: Exception) {
                    val error = errorProcessor.getDetailedError(e)
                    Log.w("Error getting PR#${pr.number}. Got: ${error.message}")
                    StatsResult.Failure(error)
                }
            }
            .filterIsInstance<StatsResult.Success>()
            .map {
                it.stats
            }

        progress.end()

        val authorPrStats = aggregatePrAuthorsPrStats(prAuthorUsedId, allMergedPrsByAuthor, mergedPrsStatsList)
        val authorReviewStats: List<AuthorReviewStats> = aggregatePrAuthorReviewStats(mergedPrsStatsList, repoId, prAuthorUsedId)

        return authorReviewStats
    }

    private fun aggregatePrAuthorReviewStats(
        mergedPrsStatsList: List<PrStats>,
        repoId: String,
        prAuthorUsedId: String
    ): List<AuthorReviewStats> {
        // Builds a map of reviewer ID to list PRs they have reviewed for the PR-Author
        val userReviews = mutableMapOf<UserId, List<ReviewStats>>()
        mergedPrsStatsList.filter { it.prApprovalTime.isNotEmpty() }
            .forEach { stats: PrStats ->
                stats.prApprovalTime.forEach { (userId, time) ->
                    val reviewStats = ReviewStats(
                        reviewerUserId = userId,
                        pullRequest = stats.pullRequest,
                        reviewCompletion = time,
                        initialResponseTime = stats.initialResponseTime[userId] ?: time,
                        prComments = stats.comments[userId] ?: noComments(userId),
                        prReadyOn = stats.prReadyOn,
                        prMergedOn = stats.prMergedOn
                    )
                    if (userReviews.containsKey(userId)) {
                        userReviews[userId] = userReviews[userId]!!.plus(reviewStats)
                    } else {
                        userReviews[userId] = listOf(reviewStats)
                    }
                }
            }

        val authorReviewStats: List<AuthorReviewStats> = userReviews.map { (reviewerUserId, reviewStats) ->
            val totalReviews: Int = reviewStats.size
            val averageReviewTime: Duration = reviewStats
                .map { it.reviewCompletion }
                .fold(Duration.ZERO, Duration::plus)
                .div(totalReviews)
            val totalReviewComments = reviewStats.sumOf { it.prComments.allComments }

            AuthorReviewStats(
                repoId = repoId,
                prAuthorId = prAuthorUsedId,
                reviewerId = reviewerUserId,
                average = averageReviewTime,
                totalReviews = totalReviews,
                totalComments = totalReviewComments,
                stats = reviewStats
            )
        }.sortedByDescending { it.totalReviews }

        Log.i("✅ Completed loading PR review stats from ${authorReviewStats.size} reviewers.")
        return authorReviewStats
    }

    private fun aggregatePrAuthorsPrStats(
        prAuthorUserId: String,
        allMergedPrsByAuthor: List<Issue>,
        mergedPrsStatsList: List<PrStats>
    ): AuthorPrStats {
        // Builds author's stats for all PRs made by the author
        val totalPrsCreated = allMergedPrsByAuthor.size
        val totalIssueComments = mergedPrsStatsList.sumOf {
            it.comments.entries.filter { prCommentEntry -> prCommentEntry.key != prAuthorUserId }.sumOf { commentEntry -> commentEntry.value.issueComment }
        }
        val totalPrSubmissionComments = mergedPrsStatsList.sumOf {
            it.comments.entries.filter { prCommentEntry -> prCommentEntry.key != prAuthorUserId }.sumOf { commentEntry -> commentEntry.value.prReviewSubmissionComment }
        }
        val totalCodeReviewComments = mergedPrsStatsList.sumOf {
            it.comments.entries.filter { prCommentEntry -> prCommentEntry.key != prAuthorUserId }.sumOf { commentEntry -> commentEntry.value.codeReviewComment }
        }

        Log.i(
            "ℹ️ The author '$prAuthorUserId' has created $totalPrsCreated PRs that successfully got merged." +
                "\nTotal Comments Received - Code Review: $totalCodeReviewComments, PR Comment: $totalIssueComments, Review+Re-review: $totalPrSubmissionComments"
        )

        return AuthorPrStats(
            authorUserId = prAuthorUserId,
            totalPrsCreated = totalPrsCreated,
            totalIssueComments = totalIssueComments,
            totalPrSubmissionComments = totalPrSubmissionComments,
            totalCodeReviewComments = totalCodeReviewComments
        )
    }
}
