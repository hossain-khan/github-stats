package dev.hossain.githubstats.formatter

import com.google.common.truth.Truth.assertThat
import dev.hossain.githubstats.AuthorPrStats
import dev.hossain.githubstats.AuthorStats
import dev.hossain.githubstats.PrStats
import dev.hossain.githubstats.ReviewStats
import dev.hossain.githubstats.ReviewerReviewStats
import dev.hossain.githubstats.UserPrComments
import dev.hossain.githubstats.formatter.html.Template
import dev.hossain.githubstats.model.PullRequest
import dev.hossain.githubstats.model.User
import dev.hossain.githubstats.util.AppConfig
import dev.hossain.githubstats.util.Config
import dev.hossain.githubstats.util.FileUtil
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import java.io.File
import kotlin.time.Duration

class HtmlChartFormatterTest : KoinTest {

    private lateinit var mockAppConfig: AppConfig
    private lateinit var formatter: HtmlChartFormatter

    // Sample data - can be simplified as we mostly check interactions with Template
    private val testUser = User("test-user", "User", "", "", "", 1L, "")
    private val testPr = PullRequest(1L, 101, "merged", "Test PR", "", "", testUser, true, "2023-01-01T00:00:00Z", "", "2023-01-02T00:00:00Z", "2023-01-02T00:00:00Z")
    private val samplePrStats = PrStats(testPr, emptyMap(), emptyMap(), emptyMap(), emptyMap(), kotlinx.datetime.Instant.DISTANT_PAST, kotlinx.datetime.Instant.DISTANT_FUTURE)
    private val sampleReviewStats = ReviewStats("reviewer-1", testPr, Duration.ZERO, Duration.ZERO, UserPrComments("",0,0,0), kotlinx.datetime.Instant.DISTANT_PAST, kotlinx.datetime.Instant.DISTANT_FUTURE, emptyMap())
    private val sampleAuthorStats = AuthorStats(
        AuthorPrStats("author-1", 1,0,0,0),
        listOf(dev.hossain.githubstats.AuthorReviewerStats("author-1", "reviewer-1", 1, 0, listOf(sampleReviewStats)))
    )
    private val sampleAggregatedPrStats = listOf(AuthorPrStats("author-1", 1, 0,0,0))
    private val sampleReviewerReviewStats = ReviewerReviewStats("reviewer-1", listOf(sampleReviewStats), mapOf("author-1" to listOf(sampleReviewStats)))

    @BeforeEach
    fun setUp() {
        mockAppConfig = mockk()

        startKoin {
            modules(module {
                single { mockAppConfig }
            })
        }

        mockkObject(FileUtil)
        mockkObject(Template) // Mock the Template object

        // Mock java.io.File constructor and writeText - this is a bit broad but helps verify file output
        mockkConstructor(File::class)
        every { anyConstructed<File>().writeText(any()) } returns Unit
        every { anyConstructed<File>().absolutePath } returns "/mocked/path/file.html"
        every { anyConstructed<File>().toURI() } returns java.net.URI("file:/mocked/path/file.html")


        formatter = HtmlChartFormatter()

        every { mockAppConfig.get() } returns Config(
            userId = "test-user", repoOwner = "owner", repoId = "repo",
            dateLimitAfter = "2023-01-01", dateLimitBefore = "2023-12-31",
            prTypes = emptyList(), authorsToIgnore = emptySet()
        )

        // FileUtil mocks
        every { FileUtil.individualPrReportHtmlChart(any()) } returns "individual-pr-chart.html"
        every { FileUtil.authorPieChartHtmlFile(any()) } returns "author-pie-chart.html"
        every { FileUtil.authorBarChartHtmlFile(any()) } returns "author-bar-chart.html"
        every { FileUtil.authorBarChartAggregateHtmlFile(any()) } returns "author-bar-aggregate.html"
        every { FileUtil.allAuthorBarChartAggregateHtmlFile() } returns "all-author-aggregate.html"
        every { FileUtil.prReviewedForCombinedBarChartFilename(any()) } returns "reviewer-combined-bar.html"
        every { FileUtil.prReviewerReviewedPrStatsBarChartFile(any()) } returns "reviewer-pr-bar.html"

        // Template mocks - verify these are called with some captured data
        every { Template.pieChart(any(), any()) } returns "mocked-pie-chart-html"
        every { Template.barChart(any(), any(), any(), any(), any()) } returns "mocked-bar-chart-html"
    }

    @AfterEach
    fun tearDown() {
        stopKoin()
        unmockkAll()
    }

    @Test
    fun `formatSinglePrStats writes empty chart initially`() {
        val result = formatter.formatSinglePrStats(samplePrStats)
        val filePathSlot = slot<String>()
        verify { anyConstructed<File>().writeText("") } // Currently writes an empty string
        assertThat(result).isEqualTo("") // Returns empty string
    }

    @Test
    fun `formatAuthorStats with empty stats returns error message`() {
        val emptyAuthorStats = sampleAuthorStats.copy(reviewStats = emptyList())
        val result = formatter.formatAuthorStats(emptyAuthorStats)
        assertThat(result).contains("⚠ ERROR: No author stats to format.")
    }

    @Test
    fun `formatAuthorStats generates charts and returns success message`() {
        val result = formatter.formatAuthorStats(sampleAuthorStats)

        verify { FileUtil.authorPieChartHtmlFile("author-1") }
        verify { FileUtil.authorBarChartHtmlFile("author-1") }
        verify { FileUtil.authorBarChartAggregateHtmlFile("author-1") }

        verify { Template.pieChart(title = any(), statsJsData = any()) }
        verify(exactly = 2) { Template.barChart(title = any(), chartData = any(), dataSize = any(), hAxisTitle = any(), vAxisTitle = any()) } // Called for two bar charts

        // Verify files are written
        verify { File(eq("author-pie-chart.html")).writeText("mocked-pie-chart-html") }
        verify { File(eq("author-bar-chart.html")).writeText("mocked-bar-chart-html") }
        verify { File(eq("author-bar-aggregate.html")).writeText("mocked-bar-chart-html") }


        assertThat(result).startsWith("📊 Written following charts for user: author-1.")
        assertThat(result).contains("file:///mocked/path/file.html") // Due to absolutePath mock
    }

    @Test
    fun `formatAllAuthorStats generates chart and returns success message`() {
        val result = formatter.formatAllAuthorStats(sampleAggregatedPrStats)

        verify { FileUtil.allAuthorBarChartAggregateHtmlFile() }
        verify { Template.barChart(title = any(), chartData = any(), dataSize = any(), hAxisTitle = any(), vAxisTitle = any()) }
        verify { File(eq("all-author-aggregate.html")).writeText("mocked-bar-chart-html") }

        assertThat(result).startsWith("📊 Written following aggregated chat for repository:")
        assertThat(result).contains("file:/mocked/path/file.html") // Due to toURI mock
    }

    @Test
    fun `formatReviewerStats with empty stats returns error message`() {
        val emptyReviewerStats = sampleReviewerReviewStats.copy(reviewedPrStats = emptyList())
        val result = formatter.formatReviewerStats(emptyReviewerStats)
        assertThat(result).contains("⚠ ERROR: No reviewer stats to format.")

        val emptyReviewedForStats = sampleReviewerReviewStats.copy(reviewedForPrStats = emptyMap())
        val result2 = formatter.formatReviewerStats(emptyReviewedForStats)
        assertThat(result2).contains("⚠ ERROR: No reviewer stats to format.")
    }

    @Test
    fun `formatReviewerStats generates charts and returns success message`() {
        val result = formatter.formatReviewerStats(sampleReviewerReviewStats)

        verify { FileUtil.prReviewedForCombinedBarChartFilename("reviewer-1") }
        verify { FileUtil.prReviewerReviewedPrStatsBarChartFile("reviewer-1") }

        verify(exactly = 2) { Template.barChart(title = any(), chartData = any(), dataSize = any(), hAxisTitle = any(), vAxisTitle = any()) }

        verify { File(eq("reviewer-combined-bar.html")).writeText("mocked-bar-chart-html") }
        verify { File(eq("reviewer-pr-bar.html")).writeText("mocked-bar-chart-html") }

        assertThat(result).startsWith("📊 Written following charts for user: reviewer-1.")
        assertThat(result).contains("file:///mocked/path/file.html")
    }
}
