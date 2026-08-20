package dev.hossain.githubstats.formatter

import com.google.common.truth.Truth.assertThat
import dev.hossain.githubstats.AuthorPrStats
import dev.hossain.githubstats.AuthorReviewStats
import dev.hossain.githubstats.AuthorStats
import dev.hossain.githubstats.PrStats
import dev.hossain.githubstats.ReviewStats
import dev.hossain.githubstats.ReviewerReviewStats
import dev.hossain.githubstats.UserPrComment
import dev.hossain.githubstats.model.PullRequest
import dev.hossain.githubstats.model.User
import dev.hossain.githubstats.util.AppConfig
import dev.hossain.githubstats.util.Config
import dev.hossain.githubstats.util.LocalProperties
import dev.hossain.i18n.Resources
import dev.hossain.i18n.ResourcesImpl
import io.mockk.every
import io.mockk.mockk
import kotlinx.datetime.Instant
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import java.io.File
import java.util.ResourceBundle
import kotlin.time.Duration.Companion.minutes

/**
 * Tests for [CsvFormatter].
 */
class CsvFormatterTest {
    private lateinit var mockProps: LocalProperties
    private lateinit var mockAppConfig: AppConfig

    private val samplePullRequest =
        PullRequest(
            id = 101L,
            number = 101,
            state = "closed",
            title = "Fix critical issue in layout",
            url = "https://api.github.com/repos/testowner/testrepo/pulls/101",
            html_url = "https://github.com/testowner/testrepo/pull/101",
            user = User("testauthor", null, null, null, null, 1L, null),
            merged = true,
            created_at = "2024-01-10T10:00:00Z",
            updated_at = "2024-01-11T12:00:00Z",
            closed_at = "2024-01-11T12:00:00Z",
            merged_at = "2024-01-11T12:00:00Z",
        )

    private val sampleReviewStats =
        ReviewStats(
            reviewerUserId = "reviewer1",
            pullRequest = samplePullRequest,
            reviewCompletion = 30.minutes,
            initialResponseTime = 15.minutes,
            prComments =
                UserPrComment(
                    user = "reviewer1",
                    issueComment = 2,
                    codeReviewComment = 4,
                    prReviewSubmissionComment = 1,
                ),
            prReadyOn = Instant.parse("2024-01-10T10:00:00Z"),
            prMergedOn = Instant.parse("2024-01-11T12:00:00Z"),
        )

    private val samplePrStats =
        PrStats(
            pullRequest = samplePullRequest,
            prApprovalTime = mapOf("reviewer1" to 30.minutes),
            initialResponseTime = mapOf("reviewer1" to 15.minutes),
            comments =
                mapOf(
                    "reviewer1" to
                        UserPrComment(
                            user = "reviewer1",
                            issueComment = 2,
                            codeReviewComment = 4,
                            prReviewSubmissionComment = 1,
                        ),
                ),
            prReadyOn = Instant.parse("2024-01-10T10:00:00Z"),
            prMergedOn = Instant.parse("2024-01-11T12:00:00Z"),
        )

    @BeforeEach
    fun setUp() {
        stopKoin()
        mockProps = mockk(relaxed = true)
        mockAppConfig = mockk(relaxed = true)

        every { mockProps.getRepoId() } returns "testrepo"
        every { mockProps.getDateLimitAfter() } returns "2024-01-01"
        every { mockProps.getDateLimitBefore() } returns "2024-12-31"

        every { mockAppConfig.get() } returns
            Config(
                repoOwner = "testowner",
                repoId = "testrepo",
                userIds = listOf("testauthor", "reviewer1"),
                botUserIds = emptyList(),
                dateLimitAfter = "2024-01-01",
                dateLimitBefore = "2024-12-31",
            )

        startKoin {
            modules(
                module {
                    single<Resources> { ResourcesImpl(ResourceBundle.getBundle("strings")) }
                    single { mockProps }
                    single { mockAppConfig }
                },
            )
        }
    }

    @AfterEach
    fun tearDown() {
        stopKoin()
        // Clean up test report directories
        File(".").listFiles { _, name -> name.startsWith("REPORTS-testrepo") }?.forEach {
            it.deleteRecursively()
        }
    }

    @Test
    fun `formatSinglePrStats returns not supported message`() {
        val formatter = CsvFormatter()
        val result = formatter.formatSinglePrStats(samplePrStats)
        assertThat(result).isNotEmpty()
    }

    @Test
    fun `formatAuthorStats returns error when reviewStats is empty`() {
        val formatter = CsvFormatter()

        val emptyAuthorStats =
            AuthorStats(
                prStats =
                    AuthorPrStats(
                        authorUserId = "testauthor",
                        totalPrsCreated = 0,
                        totalCodeReviewComments = 0,
                        totalIssueComments = 0,
                        totalPrSubmissionComments = 0,
                    ),
                reviewStats = emptyList(),
            )

        val result = formatter.formatAuthorStats(emptyAuthorStats)
        assertThat(result).contains("ERROR: No stats to format. No CSV files for you!")
    }

    @Test
    fun `formatAuthorStats writes CSV files and returns success message`() {
        val formatter = CsvFormatter()

        val authorStats =
            AuthorStats(
                prStats =
                    AuthorPrStats(
                        authorUserId = "testauthor",
                        totalPrsCreated = 1,
                        totalCodeReviewComments = 4,
                        totalIssueComments = 2,
                        totalPrSubmissionComments = 1,
                    ),
                reviewStats =
                    listOf(
                        AuthorReviewStats(
                            repoId = "testrepo",
                            prAuthorId = "testauthor",
                            reviewerId = "reviewer1",
                            average = 30.minutes,
                            totalReviews = 1,
                            totalComments = 7,
                            stats = listOf(sampleReviewStats),
                        ),
                    ),
            )

        val result = formatter.formatAuthorStats(authorStats)
        assertThat(result).contains("Generated following files")

        // Verify CSV directory was created
        val dir = File("REPORTS-testrepo-testauthor")
        assertThat(dir.exists()).isTrue()
    }

    @Test
    fun `formatReviewerStats returns error when reviewedPrStats is empty`() {
        val formatter = CsvFormatter()

        val emptyReviewerStats =
            ReviewerReviewStats(
                repoId = "testrepo",
                reviewerId = "reviewer1",
                average = 0.minutes,
                totalReviews = 0,
                reviewedPrStats = emptyList(),
                reviewedForPrStats = emptyMap(),
            )

        val result = formatter.formatReviewerStats(emptyReviewerStats)
        assertThat(result).contains("ERROR: No stats to format. No CSV files for you!")
    }

    @Test
    fun `formatReviewerStats writes CSV files and returns success message`() {
        val formatter = CsvFormatter()

        val reviewerStats =
            ReviewerReviewStats(
                repoId = "testrepo",
                reviewerId = "reviewer1",
                average = 30.minutes,
                totalReviews = 1,
                reviewedPrStats = listOf(sampleReviewStats),
                reviewedForPrStats = mapOf("testauthor" to listOf(samplePrStats)),
            )

        val result = formatter.formatReviewerStats(reviewerStats)
        assertThat(result).contains("Written")

        val dir = File("REPORTS-testrepo-reviewer1")
        assertThat(dir.exists()).isTrue()
    }

    @Test
    fun `formatAllAuthorStats returns error when list is empty`() {
        val formatter = CsvFormatter()
        val result = formatter.formatAllAuthorStats(emptyList())
        assertThat(result).contains("ERROR: No aggregated stats to format. No CSV files for you!")
    }

    @Test
    fun `formatAllAuthorStats writes aggregated CSV file`() {
        val formatter = CsvFormatter()

        val authorPrStatsList =
            listOf(
                AuthorPrStats(
                    authorUserId = "testauthor1",
                    totalPrsCreated = 5,
                    totalCodeReviewComments = 10,
                    totalIssueComments = 3,
                    totalPrSubmissionComments = 7,
                ),
                AuthorPrStats(
                    authorUserId = "testauthor2",
                    totalPrsCreated = 3,
                    totalCodeReviewComments = 6,
                    totalIssueComments = 2,
                    totalPrSubmissionComments = 4,
                ),
            )

        val result = formatter.formatAllAuthorStats(authorPrStatsList)
        assertThat(result).contains("Generated following files")

        val dir = File("REPORTS-testrepo-AGGREGATED")
        assertThat(dir.exists()).isTrue()
    }
}
