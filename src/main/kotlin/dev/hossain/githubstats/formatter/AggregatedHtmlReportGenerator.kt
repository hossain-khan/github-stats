package dev.hossain.githubstats.formatter

import dev.hossain.githubstats.AuthorPrStats
import dev.hossain.githubstats.AuthorStats
import dev.hossain.githubstats.ReviewerReviewStats
import dev.hossain.githubstats.formatter.html.BootstrapTemplate
import dev.hossain.githubstats.formatter.html.ChartDataset
import dev.hossain.githubstats.util.AppConfig
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File
import kotlin.time.DurationUnit

/**
 * Generates aggregated HTML reports using Bootstrap and Chart.js instead of scattered individual files.
 * This creates a single static site that consolidates all PR statistics.
 */
class AggregatedHtmlReportGenerator : KoinComponent {
    private val appConfig: AppConfig by inject()

    private var aggregatedStats: List<AuthorPrStats> = emptyList()
    private var authorStats: List<AuthorStats> = emptyList()
    private var reviewerStats: List<ReviewerReviewStats> = emptyList()

    /**
     * Collects all the stats data for aggregated report generation
     */
    fun collectStats(
        aggregatedPrStats: List<AuthorPrStats>,
        allAuthorStats: List<AuthorStats> = emptyList(),
        allReviewerStats: List<ReviewerReviewStats> = emptyList(),
    ) {
        this.aggregatedStats = aggregatedPrStats
        this.authorStats = allAuthorStats
        this.reviewerStats = allReviewerStats
    }

    /**
     * Generates the complete aggregated HTML report
     */
    fun generateAggregatedReport(): String {
        val repoId = appConfig.get().repoId
        val dateRange = "${appConfig.get().dateLimitAfter} to ${appConfig.get().dateLimitBefore}"
        val title = "GitHub Stats Dashboard - $repoId"

        // Generate sections
        val aggregatedSectionHtml = generateAggregatedSection()
        val authorsSectionHtml = generateAuthorsSection()
        val reviewersSectionHtml = generateReviewersSection()

        val sectionsHtml =
            listOf(
                aggregatedSectionHtml,
                authorsSectionHtml,
                reviewersSectionHtml,
            ).filter { it.isNotBlank() }.joinToString("\n")

        val htmlContent =
            BootstrapTemplate.aggregatedReport(
                title = title,
                repoId = repoId,
                dateRange = dateRange,
                sectionsHtml = sectionsHtml,
            )

        // Write to file
        val outputFile = File(getAggregatedReportFilename())
        outputFile.writeText(htmlContent)

        return "📊 Generated aggregated HTML report at: ${outputFile.toURI()}"
    }

    /**
     * Generates the aggregated statistics section
     */
    private fun generateAggregatedSection(): String {
        if (aggregatedStats.isEmpty()) return ""

        val validStats = aggregatedStats.filter { !it.isEmpty() }
        if (validStats.isEmpty()) return ""

        // Prepare data for Chart.js bar chart
        val labels = validStats.map { it.authorUserId }
        val datasets =
            listOf(
                ChartDataset(
                    label = "Total PRs Created",
                    data = validStats.map { it.totalPrsCreated },
                    backgroundColor = "#36A2EB",
                ),
                ChartDataset(
                    label = "Code Review Comments",
                    data = validStats.map { it.totalCodeReviewComments },
                    backgroundColor = "#FF6384",
                ),
                ChartDataset(
                    label = "Issue Comments",
                    data = validStats.map { it.totalIssueComments },
                    backgroundColor = "#FFCE56",
                ),
                ChartDataset(
                    label = "Review Submissions",
                    data = validStats.map { it.totalPrSubmissionComments },
                    backgroundColor = "#4BC0C0",
                ),
            )

        val chartHtml =
            BootstrapTemplate.chartJsBarChart(
                canvasId = "aggregatedStatsChart",
                title = "PR Statistics by Author",
                labels = labels,
                datasets = datasets,
            )

        return BootstrapTemplate.aggregatedSection(chartHtml)
    }

    /**
     * Generates the authors statistics section
     */
    private fun generateAuthorsSection(): String {
        if (authorStats.isEmpty()) return ""

        val authorsHtml =
            authorStats
                .mapIndexed { index, stats ->
                    if (stats.reviewStats.isEmpty()) return@mapIndexed ""

                    val authorId = stats.reviewStats.first().prAuthorId

                    // Create pie chart for reviewer distribution
                    val reviewerLabels = stats.reviewStats.map { "${it.reviewerId} [${it.stats.size}]" }
                    val reviewerData = stats.reviewStats.map { it.stats.size }

                    val pieChartHtml =
                        BootstrapTemplate.chartJsPieChart(
                            canvasId = "authorPieChart$index",
                            title = "Reviewer Distribution for $authorId",
                            labels = reviewerLabels,
                            data = reviewerData,
                        )

                    // Create bar chart for reviewer comments
                    val barLabels = stats.reviewStats.map { it.reviewerId }
                    val barDatasets =
                        listOf(
                            ChartDataset(
                                label = "Total Reviews",
                                data = stats.reviewStats.map { it.totalReviews },
                                backgroundColor = "#36A2EB",
                            ),
                            ChartDataset(
                                label = "Total Comments",
                                data = stats.reviewStats.map { it.totalComments },
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
            """

                    BootstrapTemplate.statsCard(
                        title = "📝 Author: $authorId",
                        content = cardContent,
                    )
                }.filter { it.isNotBlank() }
                .joinToString("\n")

        return if (authorsHtml.isNotBlank()) {
            BootstrapTemplate.authorsSection(authorsHtml)
        } else {
            ""
        }
    }

    /**
     * Generates the reviewers statistics section
     */
    private fun generateReviewersSection(): String {
        if (reviewerStats.isEmpty()) return ""

        val reviewersHtml =
            reviewerStats
                .mapIndexed { index, stats ->
                    if (stats.reviewedPrStats.isEmpty() || stats.reviewedForPrStats.isEmpty()) {
                        return@mapIndexed ""
                    }

                    val reviewerId = stats.reviewerId

                    // Create bar chart for reviewed authors
                    val authorLabels = stats.reviewedForPrStats.keys.toList()
                    val authorData = stats.reviewedForPrStats.values.map { it.size }

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

                    // Create bar chart for review times
                    val prLabels = stats.reviewedPrStats.take(10).map { "PR #${it.pullRequest.number}" }
                    val initialResponseTimes =
                        stats.reviewedPrStats.take(10).map {
                            it.initialResponseTime.toInt(DurationUnit.MINUTES)
                        }
                    val reviewTimes =
                        stats.reviewedPrStats.take(10).map {
                            it.reviewCompletion.toInt(DurationUnit.MINUTES)
                        }

                    val timesBarChartHtml =
                        BootstrapTemplate.chartJsBarChart(
                            canvasId = "reviewerTimesChart$index",
                            title = "Review Times (Top 10 PRs)",
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
            """

                    BootstrapTemplate.statsCard(
                        title = "🔍 Reviewer: $reviewerId",
                        content = cardContent,
                    )
                }.filter { it.isNotBlank() }
                .joinToString("\n")

        return if (reviewersHtml.isNotBlank()) {
            BootstrapTemplate.reviewersSection(reviewersHtml)
        } else {
            ""
        }
    }

    /**
     * Gets the filename for the aggregated report
     */
    private fun getAggregatedReportFilename(): String {
        val repoId = appConfig.get().repoId
        return "REPORTS-$repoId-DASHBOARD.html"
    }
}
