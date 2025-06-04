package dev.hossain.githubstats.formatter

import com.google.common.truth.Truth.assertThat
import dev.hossain.githubstats.AuthorPrStats
import dev.hossain.githubstats.AuthorStats
import dev.hossain.githubstats.PrStats
import dev.hossain.githubstats.ReviewStats
import dev.hossain.githubstats.ReviewerReviewStats
import dev.hossain.githubstats.UserPrComments
import dev.hossain.githubstats.model.PullRequest
import dev.hossain.githubstats.model.User
import dev.hossain.githubstats.util.AppConfig
import dev.hossain.githubstats.util.Config
import dev.hossain.githubstats.util.FileUtil
import dev.hossain.githubstats.util.LocalProperties
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import kotlin.time.Duration
import com.github.doyaaaaaken.kotlincsv.client.CsvWriter
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter

class CsvFormatterTest : KoinTest {

    private lateinit var mockLocalProperties: LocalProperties
    private lateinit var mockAppConfig: AppConfig
    private lateinit var mockCsvWriter: CsvWriter

    private lateinit var formatter: CsvFormatter

    // Sample data
    private val testUser = User("test-user", "User", "", "", "", 1L, "")
    private val testPr = PullRequest(1L, 101, "merged", "Test PR", "", "", testUser, true, "2023-01-01T00:00:00Z", "", "2023-01-02T00:00:00Z", "2023-01-02T00:00:00Z")
    private val samplePrStats = PrStats(
        pullRequest = testPr,
        reviewTime = mapOf(testUser.login to Duration.Companion.hours(2)),
        initialResponseTime = mapOf(testUser.login to Duration.Companion.hours(1)),
        comments = mapOf(testUser.login to UserPrComments(testUser.login, 1, 2, 3)),
        reviewers = mapOf(testUser.login to "APPROVED"),
        prReadyOn = kotlinx.datetime.Instant.DISTANT_PAST,
        prMergedOn = kotlinx.datetime.Instant.DISTANT_FUTURE
    )
    private val sampleReviewStats = ReviewStats(
        reviewerId = "reviewer-1",
        pullRequest = testPr,
        reviewCompletion = Duration.Companion.hours(3),
        initialResponseTime = Duration.Companion.minutes(30),
        prComments = UserPrComments("reviewer-1", 2,1,0),
        prReadyOn = kotlinx.datetime.Instant.DISTANT_PAST,
        prMergedOn = kotlinx.datetime.Instant.DISTANT_FUTURE,
        comments = mapOf("reviewer-1" to UserPrComments("reviewer-1", 2,1,0))
    )
    private val sampleAuthorStats = AuthorStats(
        prStats = AuthorPrStats("author-user", 1, 1, 2, 3),
        reviewStats = listOf(
            dev.hossain.githubstats.AuthorReviewerStats(
                prAuthorId = "author-user",
                reviewerId = "reviewer-1",
                totalReviews = 1,
                totalComments = 5,
                stats = listOf(sampleReviewStats)
            )
        )
    )
    private val sampleAggregatedPrStats = listOf(
        AuthorPrStats("author-1", 5, 10, 8, 2),
        AuthorPrStats("author-2", 3, 6, 4, 1)
    )
    private val sampleReviewerReviewStats = ReviewerReviewStats(
        reviewerId = "reviewer-user",
        reviewedPrStats = listOf(sampleReviewStats),
        reviewedForPrStats = mapOf("author-1" to listOf(sampleReviewStats))
    )


    @BeforeEach
    fun setUp() {
        mockLocalProperties = mockk()
        mockAppConfig = mockk()
        // Mock CsvWriter behavior
        mockCsvWriter = mockk(relaxed = true)


        startKoin {
            modules(module {
                single { mockLocalProperties }
                single { mockAppConfig }
            })
        }

        mockkObject(FileUtil)
        // Mock csvWriter() DSL to return our mock CsvWriter
        // This is a common pattern for mocking DSL top-level functions if they are extensions or similar.
        // However, doyaaaaaken.csvWriter() is a top-level function, not an extension.
        // A more robust way would be to inject CsvWriterFactory if possible, or use a more testable CSV writing abstraction.
        // For now, attempting to mock the constructor of the DSL's internal writer or its actions.
        // Let's assume for now we can verify FileUtil calls and the general structure of the output string.
        // If direct csvWriter mocking is too hard, we'll focus on what is passed to it (filenames from FileUtil).

        // Mocking csvWriter() calls to do nothing or return a mock that does nothing.
        // This is tricky because it's a DSL. We'll mock the open and writeAll extension functions.
         mockkConstructor(CsvWriter::class) // For `csvWriter().open {}`
         every { anyConstructed<CsvWriter>().writeRow(any<List<Any>>()) } just Runs
         every { anyConstructed<CsvWriter>().writeAll(any(), any<String>(), any()) } just Runs


        formatter = CsvFormatter()

        // Common stubs
        every { mockLocalProperties.getDateLimitAfter() } returns "2023-01-01"
        every { mockAppConfig.get() } returns Config(
            userId = "test-user",
            repoOwner = "owner",
            repoId = "repo",
            dateLimitAfter = "2023-01-01",
            dateLimitBefore = "2023-12-31",
            prTypes = emptyList(),
            authorsToIgnore = emptySet()
        )
        every { FileUtil.allReviewersForAuthorFile(any()) } returns "combined-reviewers.csv"
        every { FileUtil.reviewedForAuthorCsvFile(any()) } returns "individual-reviewer.csv"
        every { FileUtil.repositoryAggregatedPrStatsByAuthorFilename() } returns "repo-aggregated.csv"
        every { FileUtil.prReviewedForCombinedFilename(any()) } returns "reviewer-combined.csv"
        every { FileUtil.prReviewerReviewedPrStatsFile(any()) } returns "reviewer-prs.csv"
    }

    @AfterEach
    fun tearDown() {
        stopKoin()
        unmockkAll() // Important to clean up mocks, especially object mocks and constructors
    }

    @Test
    fun `formatSinglePrStats returns not supported message`() {
        val result = formatter.formatSinglePrStats(samplePrStats)
        assertThat(result).isEqualTo("Individual PR stats is not supported for CSV export.")
    }

    @Test
    fun `formatAuthorStats with empty stats returns error message`() {
        val emptyAuthorStats = sampleAuthorStats.copy(reviewStats = emptyList())
        val result = formatter.formatAuthorStats(emptyAuthorStats)
        assertThat(result).contains("⚠ ERROR: No stats to format.")
    }

    @Test
    fun `formatAuthorStats generates files and returns success message`() {
        val result = formatter.formatAuthorStats(sampleAuthorStats)

        assertThat(result).startsWith("Generated following files:")
        assertThat(result).contains("individual-reviewer.csv")
        assertThat(result).contains("combined-reviewers.csv")

        verify { FileUtil.allReviewersForAuthorFile("author-user") }
        verify { FileUtil.reviewedForAuthorCsvFile(sampleAuthorStats.reviewStats.first()) }
        // Verify that csvWriter().writeAll and open were called (indirectly through constructor mock)
        verify(atLeast = 1) { anyConstructed<CsvWriter>().writeAll(any(), eq("combined-reviewers.csv"), append = false) } // Header for combined
        verify(atLeast = 1) { anyConstructed<CsvWriter>().writeAll(any(), eq("combined-reviewers.csv"), append = true) }  // Data for combined
        verify(atLeast = 1) { anyConstructed<CsvWriter>().open(eq("individual-reviewer.csv"), any(), any())} // For individual
    }


    @Test
    fun `formatAllAuthorStats with empty stats returns error message`() {
        val result = formatter.formatAllAuthorStats(emptyList())
        assertThat(result).contains("⚠ ERROR: No aggregated stats to format.")
    }

    @Test
    fun `formatAllAuthorStats generates file and returns success message`() {
        val result = formatter.formatAllAuthorStats(sampleAggregatedPrStats)

        assertThat(result).startsWith("Generated following files:")
        assertThat(result).contains("repo-aggregated.csv")
        verify { FileUtil.repositoryAggregatedPrStatsByAuthorFilename() }
        verify(atLeast = 1) { anyConstructed<CsvWriter>().open(eq("repo-aggregated.csv"), any(), any()) }
    }

    @Test
    fun `formatReviewerStats with empty stats returns error message`() {
        val emptyReviewerStats = sampleReviewerReviewStats.copy(reviewedPrStats = emptyList())
        val result = formatter.formatReviewerStats(emptyReviewerStats)
        assertThat(result).contains("⚠ ERROR: No stats to format.")
    }

    @Test
    fun `formatReviewerStats generates files and returns success message`() {
        val result = formatter.formatReviewerStats(sampleReviewerReviewStats)

        assertThat(result).startsWith("Written")
        assertThat(result).contains("reviewer-combined.csv")
        assertThat(result).contains("reviewer-prs.csv")

        verify { FileUtil.prReviewedForCombinedFilename("reviewer-user") }
        verify { FileUtil.prReviewerReviewedPrStatsFile("reviewer-user") }
        verify(atLeast = 1) { anyConstructed<CsvWriter>().open(eq("reviewer-combined.csv"), any(), any()) }
        verify(atLeast = 1) { anyConstructed<CsvWriter>().open(eq("reviewer-prs.csv"), any(), any()) }
    }
}
