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
import dev.hossain.githubstats.util.LocalProperties
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
import kotlin.time.Duration.Companion.minutes

/**
 * Tests for [FileWriterFormatter].
 */
class FileWriterFormatterTest {
    private lateinit var mockProps: LocalProperties
    private lateinit var mockFormatter: StatsFormatter

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
        mockFormatter = mockk(relaxed = true)

        every { mockProps.getRepoId() } returns "testrepo"

        startKoin {
            modules(
                module {
                    single { mockProps }
                },
            )
        }
    }

    @AfterEach
    fun tearDown() {
        stopKoin()
        File(".").listFiles { _, name -> name.startsWith("REPORTS-testrepo") }?.forEach {
            it.deleteRecursively()
        }
    }

    @Test
    fun `formatSinglePrStats writes formatted output to file`() {
        every { mockFormatter.formatSinglePrStats(any()) } returns "Single PR ASCII table"
        val fileWriter = FileWriterFormatter(mockFormatter)

        val result = fileWriter.formatSinglePrStats(samplePrStats)
        assertThat(result).isEmpty()

        val file = File("REPORTS-testrepo-testauthor-PRs/REPORT-PR-101.txt")
        assertThat(file.exists()).isTrue()
        assertThat(file.readText()).isEqualTo("Single PR ASCII table")
    }

    @Test
    fun `formatAuthorStats returns error when reviewStats is empty`() {
        val fileWriter = FileWriterFormatter(mockFormatter)

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

        val result = fileWriter.formatAuthorStats(emptyAuthorStats)
        assertThat(result).contains("ERROR: No author stats to format. No files to write!")
    }

    @Test
    fun `formatAuthorStats writes formatted author stats to file`() {
        every { mockFormatter.formatAuthorStats(any()) } returns "Author ASCII table"
        val fileWriter = FileWriterFormatter(mockFormatter)

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

        val result = fileWriter.formatAuthorStats(authorStats)
        assertThat(result).isEmpty()

        val file = File("REPORTS-testrepo-testauthor/REPORT_-_pr-author-testauthor-ascii.txt")
        assertThat(file.exists()).isTrue()
        assertThat(file.readText()).isEqualTo("Author ASCII table")
    }

    @Test
    fun `formatReviewerStats returns error when reviewedPrStats is empty`() {
        val fileWriter = FileWriterFormatter(mockFormatter)

        val emptyReviewerStats =
            ReviewerReviewStats(
                repoId = "testrepo",
                reviewerId = "reviewer1",
                average = 0.minutes,
                totalReviews = 0,
                reviewedPrStats = emptyList(),
                reviewedForPrStats = emptyMap(),
            )

        val result = fileWriter.formatReviewerStats(emptyReviewerStats)
        assertThat(result).contains("ERROR: No reviewer stats to format. No files to write!")
    }

    @Test
    fun `formatReviewerStats writes formatted reviewer stats to file`() {
        every { mockFormatter.formatReviewerStats(any()) } returns "Reviewer ASCII table"
        val fileWriter = FileWriterFormatter(mockFormatter)

        val reviewerStats =
            ReviewerReviewStats(
                repoId = "testrepo",
                reviewerId = "reviewer1",
                average = 30.minutes,
                totalReviews = 1,
                reviewedPrStats = listOf(sampleReviewStats),
                reviewedForPrStats = mapOf("testauthor" to listOf(samplePrStats)),
            )

        val result = fileWriter.formatReviewerStats(reviewerStats)
        assertThat(result).isEmpty()

        val file = File("REPORTS-testrepo-reviewer1/REPORT_-_pr-reviewer-reviewer1-ascii.txt")
        assertThat(file.exists()).isTrue()
        assertThat(file.readText()).isEqualTo("Reviewer ASCII table")
    }

    @Test
    fun `formatAllAuthorStats returns empty string`() {
        val fileWriter = FileWriterFormatter(mockFormatter)
        val result = fileWriter.formatAllAuthorStats(emptyList())
        assertThat(result).isEmpty()
    }
}
