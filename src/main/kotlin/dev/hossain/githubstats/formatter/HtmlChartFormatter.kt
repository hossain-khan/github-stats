package dev.hossain.githubstats.formatter

import dev.hossain.ascii.Art
import dev.hossain.githubstats.AuthorPrStats
import dev.hossain.githubstats.AuthorStats
import dev.hossain.githubstats.PrStats
import dev.hossain.githubstats.ReviewerReviewStats
import dev.hossain.githubstats.formatter.html.Template
import dev.hossain.githubstats.util.AppConfig
import dev.hossain.githubstats.util.FileUtil
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File
import kotlin.time.DurationUnit

/**
 * Generates modern executive HTML reports and dashboards.
 * Uses Chart.js and Bootstrap 5 for modern, responsive, and dark-mode capable visual reports.
 */
class HtmlChartFormatter :
    StatsFormatter,
    KoinComponent {
    private val appConfig: AppConfig by inject()

    // Collect data for aggregated report
    private val aggregatedReportGenerator = AggregatedHtmlReportGenerator(appConfig)
    private val collectedAuthorStats = mutableListOf<AuthorStats>()
    private val collectedReviewerStats = mutableListOf<ReviewerReviewStats>()

    /**
     * Formats PR review stats for a specific single PR.
     */
    override fun formatSinglePrStats(prStats: PrStats): String {
        val formattedChart = ""

        val prStatsFileName = FileUtil.individualPrReportHtmlChart(prStats)
        File(prStatsFileName).writeText(formattedChart)

        return ""
    }

    /**
     * Formats PR review stats for list of users that reviewed specific user's PRs.
     */
    override fun formatAuthorStats(stats: AuthorStats): String {
        if (stats.reviewStats.isEmpty()) {
            return "⚠ ERROR: No author stats to format. No charts to generate! ${Art.SHRUG}"
        }

        // Collect for aggregated report
        collectedAuthorStats.add(stats)

        val prAuthorId = stats.reviewStats.first().prAuthorId
        val repoId = appConfig.get().repoId
        val dateRange = "${appConfig.get().dateLimitAfter} to ${appConfig.get().dateLimitBefore}"

        // Generate full modern Author Analytics Dashboard
        val authorDashboardHtml =
            Template.authorDashboard(
                authorId = prAuthorId,
                repoId = repoId,
                dateRange = dateRange,
                authorStats = stats,
            )

        val pieChartFileName = FileUtil.authorPieChartHtmlFile(prAuthorId)
        val pieChartFile = File(pieChartFileName)
        pieChartFile.writeText(authorDashboardHtml)

        // Prepares data for bar chart generation
        val barStatsJsData: String =
            listOf("['Reviewer', 'Total Reviewed', 'Total Commented']")
                .plus(
                    stats.reviewStats.map {
                        "['${it.reviewerId}', ${it.totalReviews}, ${it.totalComments}]"
                    },
                ).joinToString()

        val chartTitle =
            "PR reviewer's stats for PRs created by `$prAuthorId` on `$repoId` ($dateRange)"

        val formattedBarChart =
            Template.barChart(
                title = chartTitle,
                chartData = barStatsJsData,
                dataSize = stats.reviewStats.size * 2,
            )
        val barChartFileName = FileUtil.authorBarChartHtmlFile(prAuthorId)
        val barChartFile = File(barChartFileName)
        barChartFile.writeText(formattedBarChart)

        // Prepares data for bar chart with author PR's aggregate data generation
        val barStatsJsDataAggregate: String =
            listOf(
                "['PR Author', 'Total PRs Created', 'Total Source Code Review Comments Received', 'Total PR Issue Comments Received', 'Total PR Review+Re-review Submissions Received']",
            ).plus(
                "['${stats.prStats.authorUserId}', ${stats.prStats.totalPrsCreated}, ${stats.prStats.totalCodeReviewComments},${stats.prStats.totalIssueComments},${stats.prStats.totalPrSubmissionComments}]",
            ).joinToString()

        val formattedBarChartAggregate =
            Template.barChart(
                title = "PR Author Stats for `$prAuthorId` on `$repoId` ($dateRange)",
                chartData = barStatsJsDataAggregate,
                dataSize = 5,
            )
        val barChartFileNameAggregate = FileUtil.authorBarChartAggregateHtmlFile(prAuthorId)
        val barChartFileAggregate = File(barChartFileNameAggregate)
        barChartFileAggregate.writeText(formattedBarChartAggregate)

        return "📊 Written following modern dashboards for user: $prAuthorId." +
            "\n - file://${pieChartFile.absolutePath} (🌟 Full Author Dashboard)" +
            "\n - file://${barChartFileAggregate.absolutePath}" +
            "\n - file://${barChartFile.absolutePath}"
    }

    override fun formatAllAuthorStats(aggregatedPrStats: List<AuthorPrStats>): String {
        // Generate the new aggregated Bootstrap-based report
        aggregatedReportGenerator.collectStats(
            aggregatedPrStats = aggregatedPrStats,
            allAuthorStats = collectedAuthorStats,
            allReviewerStats = collectedReviewerStats,
        )

        val aggregatedReportMessage =
            try {
                aggregatedReportGenerator.generateAggregatedReport()
            } catch (e: Exception) {
                "⚠️ Failed to generate aggregated report: ${e.message}"
            }

        // Prepares data for bar chart with all author PR's aggregate data generation
        val barStatsJsDataAggregate: String =
            listOf(
                "['PR Author', 'Total PRs Created', 'Total Source Code Review Comments Received', 'Total PR Issue Comments Received', 'Total PR Review+Re-review Submissions Received']",
            ).plus(
                aggregatedPrStats.filter { it.isEmpty().not() }.map {
                    "['${it.authorUserId}', ${it.totalPrsCreated}, ${it.totalCodeReviewComments},${it.totalIssueComments},${it.totalPrSubmissionComments}]"
                },
            ).joinToString()

        val formattedBarChartAggregate =
            Template.barChart(
                title =
                    "Aggregated PR Stats on `${appConfig.get().repoId}` repository " +
                        "between ${appConfig.get().dateLimitAfter} and ${appConfig.get().dateLimitBefore}.",
                chartData = barStatsJsDataAggregate,
                dataSize = 5,
            )
        val barChartFileNameAggregate = FileUtil.allAuthorBarChartAggregateHtmlFile()
        val barChartFileAggregate = File(barChartFileNameAggregate)
        barChartFileAggregate.writeText(formattedBarChartAggregate)

        return "📊 Written following aggregated charts for repository:" +
            "\n - ${barChartFileAggregate.toURI()}" +
            "\n\n🎉 $aggregatedReportMessage"
    }

    /**
     * Formats [ReviewerReviewStats] that contains all review stats given by the reviewer.
     */
    override fun formatReviewerStats(stats: ReviewerReviewStats): String {
        if (stats.reviewedPrStats.isEmpty() || stats.reviewedForPrStats.isEmpty()) {
            return "⚠ ERROR: No reviewer stats to format. No charts to generate! ${Art.SHRUG}"
        }

        // Collect for aggregated report
        collectedReviewerStats.add(stats)

        val reviewerId = stats.reviewerId
        val repoId = appConfig.get().repoId
        val dateRange = "${appConfig.get().dateLimitAfter} to ${appConfig.get().dateLimitBefore}"

        // Generate full modern Reviewer Analytics Dashboard
        val reviewerDashboardHtml =
            Template.reviewerDashboard(
                reviewerId = reviewerId,
                repoId = repoId,
                dateRange = dateRange,
                reviewerStats = stats,
            )

        val allPrChartFileName = FileUtil.prReviewerReviewedPrStatsBarChartFile(reviewerId)
        val allPrChartFile = File(allPrChartFileName)
        allPrChartFile.writeText(reviewerDashboardHtml)

        val headerItem: List<String> =
            listOf(
                "[" +
                    "'Reviewed For different PR Authors', " +
                    "'Total PRs Reviewed by $reviewerId since ${appConfig.get().dateLimitAfter}', " +
                    "'Total Source Code Review Comments', " +
                    "'Total PR Issue Comments', " +
                    "'Total PR Review Comments', " +
                    "'Total All Comments Made'" +
                    "]",
            )

        val barStatsJsData: String =
            headerItem
                .plus(
                    stats.reviewedForPrStats.map { (prAuthorId, prReviewStats) ->
                        val userComments =
                            prReviewStats
                                .map { it.comments.values }
                                .flatten()
                                .filter { it.user == reviewerId }

                        "" +
                            "[" +
                            "'$prAuthorId', " +
                            "${prReviewStats.size}, " +
                            "${userComments.sumOf { it.codeReviewComment }}," +
                            "${userComments.sumOf { it.issueComment }}," +
                            "${userComments.sumOf { it.prReviewSubmissionComment }}," +
                            "${userComments.sumOf { it.allComments }}" +
                            "]"
                    },
                ).joinToString()

        val formattedBarChart =
            Template.barChart(
                title = "PRs Reviewed by $reviewerId ($dateRange)",
                chartData = barStatsJsData,
                dataSize = stats.reviewedForPrStats.size * 6,
            )
        val reviewedForBarChartFileName = FileUtil.prReviewedForCombinedBarChartFilename(reviewerId)
        val reviewedForBarChartFile = File(reviewedForBarChartFileName)
        reviewedForBarChartFile.writeText(formattedBarChart)

        // Update aggregated report with the collected reviewer stats
        try {
            aggregatedReportGenerator.collectStats(
                aggregatedPrStats = collectedAuthorStats.map { it.prStats },
                allAuthorStats = collectedAuthorStats,
                allReviewerStats = collectedReviewerStats,
            )
            aggregatedReportGenerator.generateAggregatedReport()
        } catch (_: Exception) {
            // Ignore if author stats are still partial
        }

        return "📊 Written following modern dashboards for user: $reviewerId." +
            "\n - file://${allPrChartFile.absolutePath} (🌟 Full Reviewer Dashboard)" +
            "\n - file://${reviewedForBarChartFile.absolutePath}"
    }
}
