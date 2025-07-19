package dev.hossain.githubstats.formatter

import dev.hossain.githubstats.AuthorPrStats
import dev.hossain.githubstats.formatter.html.BootstrapTemplate
import dev.hossain.githubstats.formatter.html.ChartDataset
import java.io.File

/**
 * Manual test to validate the aggregated HTML generation
 */
fun main() {
    println("🧪 Testing Bootstrap HTML Template Generation...")

    // Test BootstrapTemplate basic functionality
    testBootstrapTemplate()
    
    // Test Chart.js generation
    testChartJsGeneration()
    
    println("✅ All manual tests passed!")
}

fun testBootstrapTemplate() {
    val sectionsHtml = """
        <section id="test">
            <h2>Test Section</h2>
            <p>This is a test section to verify Bootstrap template works correctly.</p>
        </section>
    """

    val htmlContent = BootstrapTemplate.aggregatedReport(
        title = "Test GitHub Stats Dashboard",
        repoId = "test-repo",
        dateRange = "2025-01-01 to 2025-01-31",
        sectionsHtml = sectionsHtml
    )

    // Verify HTML contains expected elements
    assert(htmlContent.contains("Bootstrap")) { "HTML should contain Bootstrap" }
    assert(htmlContent.contains("Chart.js")) { "HTML should contain Chart.js" }
    assert(htmlContent.contains("test-repo")) { "HTML should contain repository name" }
    assert(htmlContent.contains("Test Section")) { "HTML should contain the test section" }

    // Write to test file
    val testFile = File("/tmp/test-dashboard.html")
    testFile.writeText(htmlContent)
    println("📊 Bootstrap template test passed. Generated: ${testFile.absolutePath}")
}

fun testChartJsGeneration() {
    val labels = listOf("Author1", "Author2", "Author3")
    val datasets = listOf(
        ChartDataset(
            label = "PRs Created",
            data = listOf(5, 3, 8),
            backgroundColor = "#36A2EB"
        ),
        ChartDataset(
            label = "Comments Made",
            data = listOf(12, 7, 15),
            backgroundColor = "#FF6384"
        )
    )

    val chartHtml = BootstrapTemplate.chartJsBarChart(
        canvasId = "testChart",
        title = "Test Chart",
        labels = labels,
        datasets = datasets
    )

    // Verify chart HTML contains expected elements
    assert(chartHtml.contains("Chart(")) { "Chart HTML should contain Chart.js initialization" }
    assert(chartHtml.contains("testChart")) { "Chart HTML should contain canvas ID" }
    assert(chartHtml.contains("Author1")) { "Chart HTML should contain labels" }
    assert(chartHtml.contains("PRs Created")) { "Chart HTML should contain dataset labels" }

    println("📈 Chart.js template test passed.")
}