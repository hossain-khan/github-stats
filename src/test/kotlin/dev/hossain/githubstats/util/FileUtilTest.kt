package dev.hossain.githubstats.util

import com.google.common.truth.Truth.assertThat
import dev.hossain.githubstats.AuthorReviewStats
import dev.hossain.githubstats.PrStats
import dev.hossain.githubstats.model.PullRequest
import dev.hossain.githubstats.model.User
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.datetime.Instant
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlin.time.Duration

/**
 * Unit tests for [FileUtil].
 */
class FileUtilTest {
    private lateinit var mockLocalProperties: LocalProperties

    @BeforeEach
    fun setUp() {
        mockLocalProperties = mockk(relaxed = true)
        every { mockLocalProperties.getRepoId() } returns "test-repo"

        // Setup Koin for dependency injection
        startKoin {
            modules(
                module {
                    single { mockLocalProperties }
                },
            )
        }
    }

    @AfterEach
    fun tearDown() {
        stopKoin()
        unmockkAll()
    }

    @Test
    fun `authorReportAsciiFile - generates correct filename for author`() {
        val result = FileUtil.authorReportAsciiFile("test-user")

        assertThat(result).contains("REPORTS-test-repo-test-user")
        assertThat(result).contains("REPORT_-_pr-author-test-user-ascii.txt")
    }

    @Test
    fun `authorPieChartHtmlFile - generates correct filename for author`() {
        val result = FileUtil.authorPieChartHtmlFile("test-user")

        assertThat(result).contains("REPORTS-test-repo-test-user")
        assertThat(result).contains("REPORT_-_pr-author-test-user-pie-chart.html")
    }

    @Test
    fun `authorBarChartAggregateHtmlFile - generates correct filename for author`() {
        val result = FileUtil.authorBarChartAggregateHtmlFile("test-user")

        assertThat(result).contains("REPORTS-test-repo-test-user")
        assertThat(result).contains("REPORT_-_pr-author-test-user-pr-stats-aggregate.html")
    }

    @Test
    fun `allAuthorBarChartAggregateHtmlFile - generates correct filename for all authors`() {
        val result = FileUtil.allAuthorBarChartAggregateHtmlFile()

        assertThat(result).contains("REPORTS-test-repo-AGGREGATED")
        assertThat(result).contains("REPORT_-_aggregated-pr-stats-for-all-authors.html")
    }

    @Test
    fun `authorBarChartHtmlFile - generates correct filename for author`() {
        val result = FileUtil.authorBarChartHtmlFile("test-user")

        assertThat(result).contains("REPORTS-test-repo-test-user")
        assertThat(result).contains("REPORT_-_pr-author-test-user-bar-chart.html")
    }

    @Test
    fun `reviewerReportAsciiFile - generates correct filename for reviewer`() {
        val result = FileUtil.reviewerReportAsciiFile("test-reviewer")

        assertThat(result).contains("REPORTS-test-repo-test-reviewer")
        assertThat(result).contains("REPORT_-_pr-reviewer-test-reviewer-ascii.txt")
    }

    @Test
    fun `individualPrReportAsciiFile - generates correct filename for PR`() {
        val prStats = createMockPrStats(123, "test-author")

        val result = FileUtil.individualPrReportAsciiFile(prStats)

        assertThat(result).contains("REPORTS-test-repo-test-author-PRs")
        assertThat(result).contains("REPORT-PR-123.txt")
    }

    @Test
    fun `individualPrReportHtmlChart - generates correct filename for PR`() {
        val prStats = createMockPrStats(456, "test-author")

        val result = FileUtil.individualPrReportHtmlChart(prStats)

        assertThat(result).contains("REPORTS-test-repo-test-author-PRs")
        assertThat(result).contains("REPORT-PR-456.html")
    }

    @Test
    fun `prReviewedForCombinedBarChartFilename - generates correct filename for reviewer`() {
        val result = FileUtil.prReviewedForCombinedBarChartFilename("test-reviewer")

        assertThat(result).contains("REPORTS-test-repo-test-reviewer")
        assertThat(result).contains("REPORT_-_prs-reviewed-for-authors-by-test-reviewer-bar-chart.html")
    }

    @Test
    fun `prReviewedForCombinedFilename - generates correct filename for reviewer`() {
        val result = FileUtil.prReviewedForCombinedFilename("test-reviewer")

        assertThat(result).contains("REPORTS-test-repo-test-reviewer")
        assertThat(result).contains("REPORT_-_prs-reviewed-for-authors-by-test-reviewer.csv")
    }

    @Test
    fun `repositoryAggregatedPrStatsByAuthorFilename - generates correct filename`() {
        val result = FileUtil.repositoryAggregatedPrStatsByAuthorFilename()

        assertThat(result).contains("REPORTS-test-repo-AGGREGATED")
        assertThat(result).contains("REPORT_-_aggregated-pr-stats-for-all-authors.csv")
    }

    @Test
    fun `prReviewerReviewedPrStatsFile - generates correct filename for reviewer`() {
        val result = FileUtil.prReviewerReviewedPrStatsFile("test-reviewer")

        assertThat(result).contains("REPORTS-test-repo-test-reviewer")
        assertThat(result).contains("REPORT_-_all-prs-reviewed-by-test-reviewer.csv")
    }

    @Test
    fun `prReviewerReviewedPrStatsBarChartFile - generates correct filename for reviewer`() {
        val result = FileUtil.prReviewerReviewedPrStatsBarChartFile("test-reviewer")

        assertThat(result).contains("REPORTS-test-repo-test-reviewer")
        assertThat(result).contains("REPORT_-_all-prs-reviewed-by-test-reviewer-bar-chart.html")
    }

    @Test
    fun `reviewedForAuthorCsvFile - generates correct filename for author and reviewer stats`() {
        val authorStats =
            AuthorReviewStats(
                repoId = "test-repo",
                prAuthorId = "test-author",
                reviewerId = "test-reviewer",
                average = Duration.ZERO,
                totalReviews = 0,
                totalComments = 0,
                stats = emptyList(),
            )

        val result = FileUtil.reviewedForAuthorCsvFile(authorStats)

        assertThat(result).contains("REPORTS-test-repo-test-author")
        assertThat(result).contains("REPORT-all-prs-reviewed-by-test-reviewer-for-test-author.csv")
    }

    @Test
    fun `allReviewersForAuthorFile - generates correct filename for author`() {
        val result = FileUtil.allReviewersForAuthorFile("test-author")

        assertThat(result).contains("REPORTS-test-repo-test-author")
        assertThat(result).contains("REPORT_-_test-author-all-reviewers-total-prs-reviewed.csv")
    }

    @Test
    fun `httpCacheDir - returns http-cache directory`() {
        mockkObject(FileUtil)

        val result = FileUtil.httpCacheDir()

        assertThat(result.name).isEqualTo("http-cache")
    }

    private fun createMockPrStats(
        prNumber: Int,
        authorLogin: String,
    ): PrStats {
        val user =
            User(
                login = authorLogin,
                id = 123,
                type = "User",
                url = "https://api.github.com/users/$authorLogin",
                html_url = "https://github.com/$authorLogin",
                avatar_url = "https://avatars.githubusercontent.com/u/123",
                repos_url = "https://api.github.com/users/$authorLogin/repos",
            )
        val pullRequest =
            PullRequest(
                id = 1000 + prNumber.toLong(),
                number = prNumber,
                title = "Test PR",
                url = "https://api.github.com/repos/test/repo/pulls/$prNumber",
                html_url = "https://github.com/test/repo/pull/$prNumber",
                state = "closed",
                user = user,
                merged = true,
                created_at = "2024-01-01T00:00:00Z",
                updated_at = "2024-01-02T00:00:00Z",
                closed_at = "2024-01-03T00:00:00Z",
                merged_at = "2024-01-03T00:00:00Z",
            )

        return PrStats(
            pullRequest = pullRequest,
            prReadyOn = Instant.parse("2024-01-01T00:00:00Z"),
            prMergedOn = Instant.parse("2024-01-03T00:00:00Z"),
            prApprovalTime = emptyMap(),
            initialResponseTime = emptyMap(),
            comments = emptyMap(),
        )
    }
}
