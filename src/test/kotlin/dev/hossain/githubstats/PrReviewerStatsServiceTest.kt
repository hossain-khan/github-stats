package dev.hossain.githubstats

import com.google.common.truth.Truth.assertThat
import dev.hossain.githubstats.repository.PullRequestStatsRepo
import dev.hossain.githubstats.repository.PullRequestStatsRepo.StatsResult
import dev.hossain.githubstats.service.IssueSearchPagerService
import dev.hossain.githubstats.util.AppConfig
import dev.hossain.githubstats.util.Config
import dev.hossain.githubstats.util.ErrorInfo
import dev.hossain.githubstats.util.ErrorProcessor
import dev.hossain.githubstats.model.Issue
import dev.hossain.githubstats.model.User
import dev.hossain.githubstats.model.IssuePullRequest
import dev.hossain.githubstats.model.PullRequest
import dev.hossain.i18n.Resources
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import me.tongfei.progressbar.ProgressBar
import me.tongfei.progressbar.ProgressBarBuilder
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlin.time.Duration

class PrReviewerStatsServiceTest {
    private lateinit var pullRequestStatsRepo: PullRequestStatsRepo
    private lateinit var issueSearchPager: IssueSearchPagerService
    private lateinit var appConfig: AppConfig
    private lateinit var errorProcessor: ErrorProcessor
    private lateinit var resources: Resources
    private lateinit var progressBarBuilder: ProgressBarBuilder
    private lateinit var progressBar: ProgressBar
    private lateinit var service: PrReviewerStatsService

    @BeforeEach
    fun setUp() {
        stopKoin()
        pullRequestStatsRepo = mockk()
        issueSearchPager = mockk()
        appConfig = mockk()
        errorProcessor = mockk()
        resources = mockk(relaxed = true)
        progressBarBuilder = mockk()
        progressBar = mockk(relaxed = true)

        every { progressBarBuilder.setInitialMax(any()) } returns progressBarBuilder
        every { progressBarBuilder.build() } returns progressBar

        startKoin {
            modules(module {
                single { resources }
                factory { progressBarBuilder }
            })
        }

        service = PrReviewerStatsService(
            pullRequestStatsRepo = pullRequestStatsRepo,
            issueSearchPager = issueSearchPager,
            appConfig = appConfig,
            errorProcessor = errorProcessor
        )

        every { appConfig.get() } returns Config(
            repoOwner = "owner",
            repoId = "repo",
            userIds = listOf("reviewer"),
            botUserIds = listOf("bot"),
            dateLimitAfter = "2021-01-01",
            dateLimitBefore = "2021-12-31"
        )

        every { resources.string(any(), *anyVararg()) } returns "Test string"
    }

    @AfterEach
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `reviewerStats - successfully aggregates stats for a reviewer`() = runTest {
        val reviewerId = "reviewer"
        val authorId = "author"
        val pr1 = createIssue(1, authorId)

        coEvery { issueSearchPager.searchIssues(any()) } returns listOf(pr1)

        val stats1 = createPrStats(1, authorId, mapOf(reviewerId to Duration.parse("1h")))

        coEvery { pullRequestStatsRepo.stats(any(), any(), 1, any()) } returns StatsResult.Success(stats1)

        val result = service.reviewerStats(reviewerId)

        assertThat(result.totalReviews).isEqualTo(1)
        assertThat(result.average).isEqualTo(Duration.parse("1h"))
        assertThat(result.reviewedForPrStats).containsKey(authorId)
    }

    @Test
    fun `reviewerStats - correctly filters PRs where the reviewer actually provided approval`() = runTest {
        val reviewerId = "reviewer"
        val authorId = "author"
        val pr1 = createIssue(1, authorId)
        val pr2 = createIssue(2, authorId)

        coEvery { issueSearchPager.searchIssues(any()) } returns listOf(pr1, pr2)

        val stats1 = createPrStats(1, authorId, mapOf(reviewerId to Duration.parse("1h")))
        val stats2 = createPrStats(2, authorId, mapOf("other_reviewer" to Duration.parse("2h")))

        coEvery { pullRequestStatsRepo.stats(any(), any(), 1, any()) } returns StatsResult.Success(stats1)
        coEvery { pullRequestStatsRepo.stats(any(), any(), 2, any()) } returns StatsResult.Success(stats2)

        val result = service.reviewerStats(reviewerId)

        assertThat(result.totalReviews).isEqualTo(1)
        assertThat(result.reviewedPrStats[0].pullRequest.number).isEqualTo(1)
    }

    @Test
    fun `reviewerStats - handles empty results`() = runTest {
        val reviewerId = "reviewer"
        coEvery { issueSearchPager.searchIssues(any()) } returns emptyList()

        val result = service.reviewerStats(reviewerId)

        assertThat(result.totalReviews).isEqualTo(0)
        assertThat(result.average).isEqualTo(Duration.ZERO)
    }

    private fun createIssue(number: Int, authorId: String) = Issue(
        id = number.toLong(),
        number = number,
        state = "closed",
        title = "PR $number",
        url = "url",
        html_url = "html_url",
        user = User(authorId, null, null, null, null, 1L, null),
        merged = true,
        created_at = "2021-01-01T00:00:00Z",
        updated_at = null,
        closed_at = null,
        pull_request = IssuePullRequest("url", "html_url", "diff_url", "patch_url", "2021-01-01T01:00:00Z")
    )

    private fun createPrStats(number: Int, authorId: String, reviewerStats: Map<String, Duration>) = PrStats(
        pullRequest = PullRequest(
            id = number.toLong(),
            number = number,
            state = "closed",
            title = "PR $number",
            url = "url",
            html_url = "html_url",
            user = User(authorId, null, null, null, null, 1L, null),
            merged = true,
            created_at = "2021-01-01T00:00:00Z",
            updated_at = null,
            closed_at = null,
            merged_at = "2021-01-01T01:00:00Z"
        ),
        prApprovalTime = reviewerStats,
        initialResponseTime = reviewerStats,
        comments = reviewerStats.keys.associateWith { UserPrComment(it, 1, 1, 1) },
        prReadyOn = Instant.parse("2021-01-01T00:00:00Z"),
        prMergedOn = Instant.parse("2021-01-01T01:00:00Z")
    )
}
