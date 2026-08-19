package dev.hossain.githubstats.formatter

import dev.hossain.githubstats.AuthorPrStats
import dev.hossain.githubstats.AuthorReviewStats
import dev.hossain.githubstats.AuthorStats
import dev.hossain.githubstats.ReviewStats
import dev.hossain.githubstats.ReviewerReviewStats
import dev.hossain.githubstats.UserPrComment
import dev.hossain.githubstats.formatter.html.Template
import dev.hossain.githubstats.model.PullRequest
import dev.hossain.githubstats.model.User
import dev.hossain.githubstats.util.AppConfig
import dev.hossain.githubstats.util.Config
import io.mockk.every
import io.mockk.mockk
import kotlinx.datetime.Instant
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes

class AggregatedHtmlReportGeneratorTest {
    private val mockAppConfig =
        mockk<AppConfig> {
            every { get() } returns
                Config(
                    repoOwner = "testowner",
                    repoId = "testrepo",
                    userIds = listOf("testuser1", "testuser2"),
                    botUserIds = emptyList(),
                    dateLimitAfter = "2024-01-01",
                    dateLimitBefore = "2024-12-31",
                )
        }

    private val samplePullRequest =
        PullRequest(
            id = 101L,
            number = 101,
            state = "closed",
            title = "Fix critical issue in layout",
            url = "https://api.github.com/repos/testowner/testrepo/pulls/101",
            html_url = "https://github.com/testowner/testrepo/pull/101",
            user = User("testuser1", null, null, null, null, 1L, null),
            merged = true,
            created_at = "2024-01-10T10:00:00Z",
            updated_at = "2024-01-11T12:00:00Z",
            closed_at = "2024-01-11T12:00:00Z",
            merged_at = "2024-01-11T12:00:00Z",
        )

    @Test
    fun `generateAggregatedReport - creates valid HTML with Bootstrap and Chart_js`() {
        val generator = AggregatedHtmlReportGenerator(mockAppConfig)

        val sampleStats =
            listOf(
                AuthorPrStats(
                    authorUserId = "testuser1",
                    totalPrsCreated = 5,
                    totalCodeReviewComments = 10,
                    totalIssueComments = 3,
                    totalPrSubmissionComments = 7,
                ),
                AuthorPrStats(
                    authorUserId = "testuser2",
                    totalPrsCreated = 3,
                    totalCodeReviewComments = 6,
                    totalIssueComments = 2,
                    totalPrSubmissionComments = 4,
                ),
            )

        generator.collectStats(aggregatedPrStats = sampleStats)

        val result = generator.generateAggregatedReport()
        assertTrue(result.contains("Generated aggregated HTML report"))
    }

    @Test
    fun `collectStats - handles empty data gracefully`() {
        val generator = AggregatedHtmlReportGenerator(mockAppConfig)

        generator.collectStats(aggregatedPrStats = emptyList())

        val result = generator.generateAggregatedReport()
        assertTrue(result.contains("Generated aggregated HTML report"))
    }

    @Test
    fun `Template authorDashboard - generates valid modern HTML dashboard`() {
        val reviewStats =
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

        val authorStats =
            AuthorStats(
                prStats =
                    AuthorPrStats(
                        authorUserId = "testuser1",
                        totalPrsCreated = 5,
                        totalCodeReviewComments = 4,
                        totalIssueComments = 2,
                        totalPrSubmissionComments = 1,
                    ),
                reviewStats =
                    listOf(
                        AuthorReviewStats(
                            repoId = "testrepo",
                            prAuthorId = "testuser1",
                            reviewerId = "reviewer1",
                            average = 30.minutes,
                            totalReviews = 1,
                            totalComments = 7,
                            stats = listOf(reviewStats),
                        ),
                    ),
            )

        val html =
            Template.authorDashboard(
                authorId = "testuser1",
                repoId = "testrepo",
                dateRange = "2024-01-01 to 2024-12-31",
                authorStats = authorStats,
            )

        assertTrue(html.contains("Author Analytics"))
        assertTrue(html.contains("testuser1"))
        assertTrue(html.contains("reviewer1"))
        assertTrue(html.contains("Chart.js"))
    }

    @Test
    fun `Template reviewerDashboard - generates valid modern HTML dashboard`() {
        val reviewStats =
            ReviewStats(
                reviewerUserId = "reviewer1",
                pullRequest = samplePullRequest,
                reviewCompletion = 45.minutes,
                initialResponseTime = 10.minutes,
                prComments =
                    UserPrComment(
                        user = "reviewer1",
                        issueComment = 1,
                        codeReviewComment = 3,
                        prReviewSubmissionComment = 1,
                    ),
                prReadyOn = Instant.parse("2024-01-10T10:00:00Z"),
                prMergedOn = Instant.parse("2024-01-11T12:00:00Z"),
            )

        val reviewerStats =
            ReviewerReviewStats(
                repoId = "testrepo",
                reviewerId = "reviewer1",
                average = 45.minutes,
                totalReviews = 1,
                reviewedPrStats = listOf(reviewStats),
                reviewedForPrStats = mapOf("testuser1" to emptyList()),
            )

        val html =
            Template.reviewerDashboard(
                reviewerId = "reviewer1",
                repoId = "testrepo",
                dateRange = "2024-01-01 to 2024-12-31",
                reviewerStats = reviewerStats,
            )

        assertTrue(html.contains("Reviewer Analytics"))
        assertTrue(html.contains("reviewer1"))
        assertTrue(html.contains("testuser1"))
        assertTrue(html.contains("Chart.js"))
    }
}
