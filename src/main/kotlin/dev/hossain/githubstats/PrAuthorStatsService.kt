package dev.hossain.githubstats

import dev.hossain.githubstats.UserPrComment.Companion.empty
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
import org.koin.core.component.KoinComponent
import kotlin.time.Duration

/**
 * Generates PR stats for all the PRs created by the specific author.
 *
 * @see PrReviewerStatsService
 */
class PrAuthorStatsService constructor(
    private val pullRequestStatsRepo: PullRequestStatsRepo,
    private val appConfig: AppConfig,
    private val errorProcessor: ErrorProcessor
) : KoinComponent {

    /**
     * Generates stats for reviews given by different PR reviewers for specified PR [prAuthorUsedId].
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
        prAuthorUsedId: String
    ): List<AuthorReviewStats> {
        val (repoOwner, repoId, _, dateLimitAfter, dateLimitBefore) = appConfig.get()

        // First get all the recent PRs made by author
        val issueSearchPager: IssueSearchPagerService = getKoin().get()
        val closedPrs: List<Issue> = issueSearchPager.searchIssues(
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
        val progress = PrAnalysisProgress(closedPrs).also { it.start() }

        // For each PR by author, get the review stats on the PR
        val prStatsList: List<PrStats> = closedPrs
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

        // Builds a map of reviewer ID to list PRs they have reviewed for the PR-Author
        val userReviews = mutableMapOf<UserId, List<ReviewStats>>()
        prStatsList.filter { it.reviewTime.isNotEmpty() }
            .forEach { stats: PrStats ->
                stats.reviewTime.forEach { (user, time) ->
                    val reviewStats = ReviewStats(
                        pullRequest = stats.pullRequest,
                        reviewCompletion = time,
                        prComments = stats.comments[user] ?: empty(user),
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

        val authorReviewStats: List<AuthorReviewStats> = userReviews.map { (reviewerUserId, reviewStats) ->
            val totalReviews: Int = reviewStats.size
            val averageReviewTime: Duration = reviewStats
                .map { it.reviewCompletion }
                .fold(Duration.ZERO, Duration::plus)
                .div(totalReviews)

            AuthorReviewStats(
                repoId = repoId,
                prAuthorId = prAuthorUsedId,
                reviewerId = reviewerUserId,
                average = averageReviewTime,
                totalReviews = totalReviews,
                stats = reviewStats
            )
        }.sortedByDescending { it.totalReviews }

        Log.i("✅ Completed loading PR review stats from ${authorReviewStats.size} reviewers.")

        return authorReviewStats
    }
}
