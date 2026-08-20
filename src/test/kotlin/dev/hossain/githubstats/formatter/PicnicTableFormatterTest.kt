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
import java.util.ResourceBundle
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

/**
 * Tests for [PicnicTableFormatter].
 */
class PicnicTableFormatterTest {
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
        mockAppConfig = mockk(relaxed = true)

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
                    single { mockAppConfig }
                },
            )
        }
    }

    @AfterEach
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `formatSinglePrStats formats single PR into picnic table`() {
        val formatter = PicnicTableFormatter()

        val table = formatter.formatSinglePrStats(samplePrStats)
        assertThat(table).contains("Fix critical issue in layout")
        assertThat(table).contains("testauthor")
        assertThat(table).contains("reviewer1")
    }

    @Test
    fun `formatAuthorStats returns error when reviewStats is empty`() {
        val formatter = PicnicTableFormatter()

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
        assertThat(result).contains("ERROR: No stats to format. No ◫ fancy tables for you!")
    }

    @Test
    fun `formatAuthorStats formats author stats with reviewer tables`() {
        val formatter = PicnicTableFormatter()

        val authorStats =
            AuthorStats(
                prStats =
                    AuthorPrStats(
                        authorUserId = "testauthor",
                        totalPrsCreated = 2,
                        totalCodeReviewComments = 8,
                        totalIssueComments = 4,
                        totalPrSubmissionComments = 2,
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
        assertThat(result).contains("testauthor")
        assertThat(result).contains("reviewer1")
    }

    @Test
    fun `formatReviewerStats returns error when reviewedPrStats is empty`() {
        val formatter = PicnicTableFormatter()

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
        assertThat(result).contains("ERROR: No stats to format. No ◫ fancy tables for you!")
    }

    @Test
    fun `formatReviewerStats formats reviewer tables for all PRs and authors`() {
        val formatter = PicnicTableFormatter()

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
        assertThat(result).contains("reviewer1")
        assertThat(result).contains("testauthor")
    }

    @Test
    fun `formatAllAuthorStats returns empty string`() {
        val formatter = PicnicTableFormatter()
        val result = formatter.formatAllAuthorStats(emptyList())
        assertThat(result).isEmpty()
    }
}
