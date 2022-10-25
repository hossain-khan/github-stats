package dev.hossain.githubstats

import dev.hossain.githubstats.AppConstants.PROGRESS_LABEL
import dev.hossain.githubstats.model.Issue
import dev.hossain.githubstats.repository.PullRequestStatsRepo
import dev.hossain.githubstats.repository.PullRequestStatsRepo.StatsResult
import dev.hossain.githubstats.service.IssueSearchPager
import dev.hossain.githubstats.service.SearchParams
import dev.hossain.githubstats.util.ErrorProcessor
import kotlinx.coroutines.delay
import me.tongfei.progressbar.ConsoleProgressBarConsumer
import me.tongfei.progressbar.ProgressBarBuilder
import me.tongfei.progressbar.ProgressBarStyle
import org.koin.core.component.KoinComponent
import kotlin.time.Duration

/**
 * Generates PR stats for all the PRs created by the specific author.
 *
 * @see PrReviewerStatsService
 */
class PrAuthorStatsService constructor(
    private val pullRequestStatsRepo: PullRequestStatsRepo,
    private val errorProcessor: ErrorProcessor
) : KoinComponent {

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
        dateLimit: String
    ): List<AuthorReviewStats> {
        // First get all the recent PRs made by author
        val issueSearchPager: IssueSearchPager = getKoin().get()
        val closedPrs: List<Issue> = issueSearchPager.searchIssues(
            searchQuery = SearchParams(repoOwner = owner, repoId = repo, author = author, dateAfter = dateLimit).toQuery()
        ).filter {
            // Makes sure it is a PR, not an issue
            it.pull_request != null
        }

        // Provides periodic progress updates based on config
        val progressBar = ProgressBarBuilder()
            .setTaskName(PROGRESS_LABEL)
            .setStyle(ProgressBarStyle.COLORFUL_UNICODE_BAR)
            .setConsumer(ConsoleProgressBarConsumer(System.out))
            .setInitialMax(closedPrs.size.toLong())
            .build()

        // For each PR by author, get the review stats on the PR
        val prStatsList: List<PrStats> = closedPrs
            .mapIndexed { index, pr ->
                if (index.rem(10) == 0) {
                    progressBar.stepTo(index + 1L)
                }
                delay(BuildConfig.API_REQUEST_DELAY_MS) // Slight delay to avoid per-second limit
                try {
                    pullRequestStatsRepo.stats(
                        repoOwner = owner,
                        repoId = repo,
                        prNumber = pr.number
                    )
                } catch (e: Exception) {
                    println("Error getting PR#${pr.number}. Got: ${e.message}")
                    StatsResult.Failure(errorProcessor.getDetailedError(e))
                }
            }
            .filterIsInstance<StatsResult.Success>()
            .map {
                it.stats
            }

        progressBar.close()

        // Builds a map of reviewer ID to list PRs they have reviewed for the PR-Author
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

        val authorReviewStats: List<AuthorReviewStats> = userReviews.map { (reviewerUserId, reviewStats) ->
            val totalReviews: Int = reviewStats.size
            val averageReviewTime: Duration = reviewStats
                .map { it.reviewCompletion }
                .fold(Duration.ZERO, Duration::plus)
                .div(totalReviews)

            AuthorReviewStats(
                repoId = repo,
                prAuthorId = author,
                reviewerId = reviewerUserId,
                average = averageReviewTime,
                totalReviews = totalReviews,
                stats = reviewStats
            )
        }.sortedByDescending { it.totalReviews }

        if (BuildConfig.DEBUG) {
            println("✅ Completed loading PR review stats from ${authorReviewStats.size} reviewers.")
        }

        return authorReviewStats
    }
}
