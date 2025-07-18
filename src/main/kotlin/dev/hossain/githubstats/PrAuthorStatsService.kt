package dev.hossain.githubstats

import dev.hossain.githubstats.UserPrComment.Companion.noComments
import dev.hossain.githubstats.logging.Log
import dev.hossain.githubstats.model.Issue
import dev.hossain.githubstats.repository.PullRequestStatsRepo
import dev.hossain.githubstats.repository.PullRequestStatsRepo.StatsResult
import dev.hossain.githubstats.service.IssueSearchPagerService
import dev.hossain.githubstats.service.SearchParams
import dev.hossain.githubstats.util.AppConfig
import dev.hossain.githubstats.util.ErrorInfo
import dev.hossain.githubstats.util.ErrorProcessor
import dev.hossain.githubstats.util.ErrorThreshold
import dev.hossain.githubstats.util.PrAnalysisProgress
import dev.hossain.githubstats.util.RateLimitHandler
import dev.hossain.i18n.Resources
import kotlinx.coroutines.delay
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
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
    private val errorProcessor: ErrorProcessor,
    private val rateLimitHandler: RateLimitHandler = RateLimitHandler(),
) : KoinComponent {
    private val resources: Resources by inject()

    /**
     * Keep count of error received during the process.
     * Key: Error message, Value: Count of occurrence.
     */
    private val errorMap = mutableMapOf<String, Int>()

    /**
     * Generates stats for reviews given by different PR reviewers for specified PR [prAuthorUserId].
     *
     * For example, assume 'Bob' is a contributor on a specific repo called 'Awesome Json Library'.
     * This will be generated PR reviews for all the PRs 'Bob' has created and will be grouped by
     * all the PR reviewers like 'Sally', 'Mike', 'Jim' and so on.
     *
     * NOTE: If [AppConfig.botUserIds] is defined, then those users will be excluded from the review stats.
     *
     * ```
     * Sally -> 78 PRs reviewed for Bob; Average Review Time: 2 hours 8 min
     * Mike -> 42 PRs reviewed for Bob; Average Review Time: 8 hours 32 min
     * Jim -> 13 PRs reviewed for Bob; Average Review Time: 14 hours 21 min
     * ```
     */
    suspend fun authorStats(prAuthorUserId: String): AuthorStats {
        val (repoOwner, repoId, _, botUserIds, dateLimitAfter, dateLimitBefore) = appConfig.get()

        // First get all the recent PRs made by author
        val allMergedPrsByAuthor: List<Issue> =
            issueSearchPager
                .searchIssues(
                    searchQuery =
                        SearchParams(
                            repoOwner = repoOwner,
                            repoId = repoId,
                            author = prAuthorUserId,
                            dateAfter = dateLimitAfter,
                            dateBefore = dateLimitBefore,
                        ).toQuery(),
                ).filter {
                    // Makes sure it is a PR, not an issue
                    it.pull_request != null
                }

        // Provides periodic progress updates based on config
        val progress = PrAnalysisProgress(allMergedPrsByAuthor).also { it.start() }

        // For each PR by author, get the review stats on the PR
        val mergedPrsStatsList: List<PrStats> =
            allMergedPrsByAuthor
                .mapIndexed { index, pr ->
                    progress.publish(index)

                    // Use smart rate limiting with minimum delay between requests
                    delay(rateLimitHandler.calculateDelay(null))

                    try {
                        pullRequestStatsRepo.stats(
                            repoOwner = repoOwner,
                            repoId = repoId,
                            prNumber = pr.number,
                            botUserIds = botUserIds,
                        )
                    } catch (e: Exception) {
                        val errorInfo = errorProcessor.getDetailedError(e)
                        Log.w(resources.string("error_author_pr_fetch", pr.number, errorInfo.errorMessage, errorInfo.debugGuideMessage))
                        val errorThreshold = checkErrorLimit(errorInfo)

                        if (errorThreshold.exceeded) {
                            throw RuntimeException(errorThreshold.errorMessage)
                        }

                        StatsResult.Failure(errorInfo)
                    }
                }.filterIsInstance<StatsResult.Success>()
                .map {
                    it.stats
                }

        progress.end()

        val authorPrStats = aggregatePrAuthorsPrStats(prAuthorUserId, allMergedPrsByAuthor, mergedPrsStatsList)
        Log.i(
            "ℹ️ The author '$prAuthorUserId' has created ${authorPrStats.totalPrsCreated} PRs that successfully got merged." +
                "\nTotal Comments Received - " +
                "Code Review: ${authorPrStats.totalCodeReviewComments}, " +
                "PR Comment: ${authorPrStats.totalIssueComments}, " +
                "Review+Re-review: ${authorPrStats.totalPrSubmissionComments}",
        )

        val authorReviewStats: List<AuthorReviewStats> =
            aggregatePrAuthorReviewStats(mergedPrsStatsList, repoId, prAuthorUserId)
        Log.i(resources.string("success_author_pr_loading_complete", authorReviewStats.size))

        return AuthorStats(prStats = authorPrStats, reviewStats = authorReviewStats)
    }

    private fun aggregatePrAuthorReviewStats(
        mergedPrsStatsList: List<PrStats>,
        repoId: String,
        prAuthorUsedId: String,
    ): List<AuthorReviewStats> {
        // Builds a map of reviewer ID to list PRs they have reviewed for the PR-Author
        val userReviews = mutableMapOf<UserId, List<ReviewStats>>()
        mergedPrsStatsList
            .filter { it.prApprovalTime.isNotEmpty() }
            .forEach { stats: PrStats ->
                stats.prApprovalTime.forEach { (userId, time) ->
                    val reviewStats =
                        ReviewStats(
                            reviewerUserId = userId,
                            pullRequest = stats.pullRequest,
                            reviewCompletion = time,
                            initialResponseTime = stats.initialResponseTime[userId] ?: time,
                            prComments = stats.comments[userId] ?: noComments(userId),
                            prReadyOn = stats.prReadyOn,
                            prMergedOn = stats.prMergedOn,
                        )
                    if (userReviews.containsKey(userId)) {
                        userReviews[userId] = userReviews[userId]!!.plus(reviewStats)
                    } else {
                        userReviews[userId] = listOf(reviewStats)
                    }
                }
            }

        val authorReviewStats: List<AuthorReviewStats> =
            userReviews
                .map { (reviewerUserId, reviewStats) ->
                    val totalReviews: Int = reviewStats.size
                    val averageReviewTime: Duration =
                        reviewStats
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
                        stats = reviewStats,
                    )
                }.sortedByDescending { it.totalReviews }

        return authorReviewStats
    }

    /**
     * This basically aggregates PR authors stats about PR created by the author.
     *
     * For example, if Bob had created 20 PRs that got merged into repository. That stats would include things like:
     * - Total PRs created
     * - Total PR comments received (for different types)
     * - and so on. See [AuthorPrStats] for available properties.
     */
    private fun aggregatePrAuthorsPrStats(
        prAuthorUserId: String,
        allMergedPrsByAuthor: List<Issue>,
        mergedPrsStatsList: List<PrStats>,
    ): AuthorPrStats {
        // Builds author's stats for all PRs made by the author
        val totalPrsCreated = allMergedPrsByAuthor.size
        val totalIssueComments =
            mergedPrsStatsList.sumOf {
                it.comments.entries
                    .filter { prCommentEntry -> prCommentEntry.key != prAuthorUserId }
                    .sumOf { commentEntry -> commentEntry.value.issueComment }
            }
        val totalPrSubmissionComments =
            mergedPrsStatsList.sumOf {
                it.comments.entries
                    .filter { prCommentEntry -> prCommentEntry.key != prAuthorUserId }
                    .sumOf { commentEntry -> commentEntry.value.prReviewSubmissionComment }
            }
        val totalCodeReviewComments =
            mergedPrsStatsList.sumOf {
                it.comments.entries
                    .filter { prCommentEntry -> prCommentEntry.key != prAuthorUserId }
                    .sumOf { commentEntry -> commentEntry.value.codeReviewComment }
            }

        return AuthorPrStats(
            authorUserId = prAuthorUserId,
            totalPrsCreated = totalPrsCreated,
            totalIssueComments = totalIssueComments,
            totalPrSubmissionComments = totalPrSubmissionComments,
            totalCodeReviewComments = totalCodeReviewComments,
        )
    }

    private fun checkErrorLimit(errorInfo: ErrorInfo): ErrorThreshold {
        errorMap[errorInfo.errorMessage] = errorMap.getOrDefault(errorInfo.errorMessage, 0) + 1

        val errorCount = errorMap[errorInfo.errorMessage]!!
        if (errorCount > BuildConfig.ERROR_THRESHOLD) {
            Log.w(resources.string("error_threshold_exceeded", errorInfo.errorMessage, errorCount))
            return ErrorThreshold(
                exceeded = true,
                errorMessage = resources.string("error_threshold_exceeded", errorInfo.errorMessage, errorCount),
            )
        }
        return ErrorThreshold(exceeded = false, errorMessage = "")
    }
}
