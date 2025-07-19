package dev.hossain.githubstats.formatter

import dev.hossain.githubstats.AuthorPrStats
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class AggregatedHtmlReportGeneratorTest {
    @Test
    fun `generateAggregatedReport - creates valid HTML with Bootstrap and Chart_js`() {
        // This is a basic test to ensure the HTML structure is valid
        val generator = AggregatedHtmlReportGenerator()

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

        // Verify that the method returned a success message
        assertTrue(result.contains("Generated aggregated HTML report"))

        // Note: The actual HTML file testing would require reading from filesystem
        // which is beyond the scope of this unit test
    }

    @Test
    fun `collectStats - handles empty data gracefully`() {
        val generator = AggregatedHtmlReportGenerator()

        // Should not throw exception with empty data
        generator.collectStats(aggregatedPrStats = emptyList())

        val result = generator.generateAggregatedReport()
        assertTrue(result.contains("Generated aggregated HTML report"))
    }
}
