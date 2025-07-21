package dev.hossain.githubstats.formatter

import dev.hossain.githubstats.formatter.html.BootstrapTemplate
import dev.hossain.githubstats.formatter.html.ChartDataset
import java.io.File

/**
 * Creates a comprehensive demo HTML dashboard to showcase the aggregated reporting feature
 */
fun main() {
    println("🚀 Creating comprehensive demo HTML dashboard...")

    val demoHtml = createDemoDashboard()

    // Write demo to file
    val demoFile = File("/tmp/github-stats-demo-dashboard.html")
    demoFile.writeText(demoHtml)

    println("📊 Demo dashboard created at: ${demoFile.absolutePath}")
    println("📍 Open this file in your browser to see the complete dashboard!")
    println("🎯 This demonstrates the full aggregated HTML report with Bootstrap and Chart.js")
}

fun createDemoDashboard(): String {
    val repoId = "okhttp"
    val dateRange = "2025-07-01 to 2025-07-31"
    val title = "GitHub Stats Dashboard - $repoId"

    // Create aggregated section
    val aggregatedSectionHtml = createAggregatedSection()

    // Create authors section
    val authorsSectionHtml = createAuthorsSection()

    // Create reviewers section
    val reviewersSectionHtml = createReviewersSection()

    val sectionsHtml =
        listOf(
            aggregatedSectionHtml,
            authorsSectionHtml,
            reviewersSectionHtml,
        ).joinToString("\n")

    return BootstrapTemplate.aggregatedReport(
        title = title,
        repoId = repoId,
        dateRange = dateRange,
        sectionsHtml = sectionsHtml,
    )
}

fun createAggregatedSection(): String {
    val labels = listOf("swankjesse", "yschimke", "JakeWharton", "oldergod")
    val datasets =
        listOf(
            ChartDataset(
                label = "Total PRs Created",
                data = listOf(3, 10, 0, 2),
                backgroundColor = "#36A2EB",
            ),
            ChartDataset(
                label = "Code Review Comments",
                data = listOf(2, 14, 0, 8),
                backgroundColor = "#FF6384",
            ),
            ChartDataset(
                label = "Issue Comments",
                data = listOf(1, 3, 0, 2),
                backgroundColor = "#FFCE56",
            ),
            ChartDataset(
                label = "Review Submissions",
                data = listOf(1, 7, 0, 5),
                backgroundColor = "#4BC0C0",
            ),
        )

    val chartHtml =
        BootstrapTemplate.chartJsBarChart(
            canvasId = "aggregatedStatsChart",
            title = "PR Statistics by Author - okhttp Repository",
            labels = labels,
            datasets = datasets,
        )

    return BootstrapTemplate.aggregatedSection(chartHtml)
}

fun createAuthorsSection(): String {
    val authors =
        listOf(
            createAuthorCard("swankjesse", 0),
            createAuthorCard("yschimke", 1),
        )

    return BootstrapTemplate.authorsSection(authors.joinToString("\n"))
}

fun createAuthorCard(
    authorId: String,
    index: Int,
): String {
    // Sample reviewer distribution for pie chart
    val reviewerLabels = listOf("JakeWharton", "oldergod", "yschimke")
    val reviewerData =
        when (authorId) {
            "swankjesse" -> listOf(1, 1, 0)
            "yschimke" -> listOf(1, 0, 3)
            else -> listOf(1, 1, 1)
        }

    val pieChartHtml =
        BootstrapTemplate.chartJsPieChart(
            canvasId = "authorPieChart$index",
            title = "Reviewer Distribution for $authorId",
            labels = reviewerLabels,
            data = reviewerData,
        )

    // Sample review activity bar chart
    val barLabels = reviewerLabels
    val barDatasets =
        listOf(
            ChartDataset(
                label = "Total Reviews",
                data = reviewerData,
                backgroundColor = "#36A2EB",
            ),
            ChartDataset(
                label = "Total Comments",
                data =
                    when (authorId) {
                        "swankjesse" -> listOf(3, 2, 0)
                        "yschimke" -> listOf(5, 0, 8)
                        else -> listOf(2, 3, 4)
                    },
                backgroundColor = "#FF6384",
            ),
        )

    val barChartHtml =
        BootstrapTemplate.chartJsBarChart(
            canvasId = "authorBarChart$index",
            title = "Review Activity for $authorId",
            labels = barLabels,
            datasets = barDatasets,
        )

    val cardContent = """
        $pieChartHtml
        <hr>
        $barChartHtml
        <div class="mt-3">
            <h6>Summary:</h6>
            <ul class="list-unstyled">
                <li><strong>Total PRs:</strong> ${if (authorId == "swankjesse") 3 else 10}</li>
                <li><strong>Avg Review Time:</strong> ${if (authorId == "swankjesse") "2.3 hours" else "1.8 hours"}</li>
                <li><strong>Active Reviewers:</strong> ${reviewerLabels.size}</li>
            </ul>
        </div>
    """

    return BootstrapTemplate.statsCard(
        title = "📝 Author: $authorId",
        content = cardContent,
    )
}

fun createReviewersSection(): String {
    val reviewers =
        listOf(
            createReviewerCard("JakeWharton", 0),
            createReviewerCard("oldergod", 1),
        )

    return BootstrapTemplate.reviewersSection(reviewers.joinToString("\n"))
}

fun createReviewerCard(
    reviewerId: String,
    index: Int,
): String {
    // Sample authors reviewed data
    val authorLabels = listOf("swankjesse", "yschimke", "other-authors")
    val authorData =
        when (reviewerId) {
            "JakeWharton" -> listOf(1, 1, 1)
            "oldergod" -> listOf(1, 0, 2)
            else -> listOf(2, 1, 1)
        }

    val authorBarChartHtml =
        BootstrapTemplate.chartJsBarChart(
            canvasId = "reviewerAuthorChart$index",
            title = "PRs Reviewed by Author",
            labels = authorLabels,
            datasets =
                listOf(
                    ChartDataset(
                        label = "PRs Reviewed",
                        data = authorData,
                        backgroundColor = "#4BC0C0",
                    ),
                ),
        )

    // Sample review times data
    val prLabels = listOf("PR #8929", "PR #8923", "PR #8898", "PR #8902", "PR #8915")
    val initialResponseTimes = listOf(0, 0, 0, 15, 30) // minutes
    val reviewTimes = listOf(0, 0, 0, 60, 45) // minutes

    val timesBarChartHtml =
        BootstrapTemplate.chartJsBarChart(
            canvasId = "reviewerTimesChart$index",
            title = "Review Times (Recent PRs)",
            labels = prLabels,
            datasets =
                listOf(
                    ChartDataset(
                        label = "Initial Response (mins)",
                        data = initialResponseTimes,
                        backgroundColor = "#FFCE56",
                    ),
                    ChartDataset(
                        label = "Review Completion (mins)",
                        data = reviewTimes,
                        backgroundColor = "#9966FF",
                    ),
                ),
        )

    val cardContent = """
        $authorBarChartHtml
        <hr>
        $timesBarChartHtml
        <div class="mt-3">
            <h6>Summary:</h6>
            <ul class="list-unstyled">
                <li><strong>Total Reviews:</strong> ${authorData.sum()}</li>
                <li><strong>Avg Response Time:</strong> ${if (reviewerId == "JakeWharton") "0 mins" else "22.5 mins"}</li>
                <li><strong>Authors Helped:</strong> ${authorLabels.size}</li>
            </ul>
        </div>
    """

    return BootstrapTemplate.statsCard(
        title = "🔍 Reviewer: $reviewerId",
        content = cardContent,
    )
}
