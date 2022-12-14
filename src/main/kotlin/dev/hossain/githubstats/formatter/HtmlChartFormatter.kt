package dev.hossain.githubstats.formatter

import dev.hossain.ascii.Art
import dev.hossain.githubstats.AuthorReviewStats
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
 * Generates HTML based charts for the available data.
 * Currently, it uses [Google Chart](https://developers.google.com/chart) to generate simple charts.
 */
class HtmlChartFormatter : StatsFormatter, KoinComponent {
    private val appConfig: AppConfig by inject()

    /**
     * Formats PR review stats for a specific single PR.
     */
    override fun formatSinglePrStats(prStats: PrStats): String {
        val formattedChart = ""

        val prStatsFileName = FileUtil.prReportChart(prStats)
        File(prStatsFileName).writeText(formattedChart)

        return ""
    }

    /**
     * Formats PR review stats for list of users that reviewed specific user's PRs.
     */
    override fun formatAuthorStats(stats: List<AuthorReviewStats>): String {
        if (stats.isEmpty()) {
            return "⚠ ERROR: No author stats to format. No charts to generate! ${Art.shrug}"
        }

        val prAuthorId = stats.first().prAuthorId

        // Prepares data for pie chart generation
        // https://developers.google.com/chart/interactive/docs/gallery/piechart
        val statsJsData = stats.map {
            "['${it.reviewerId} [${it.stats.size}]', ${it.stats.size}]"
        }.joinToString()

        val chartTitle = "PR reviewer`s stats for PRs created by `$prAuthorId` on `${appConfig.get().repoId}` repository " +
            "between ${appConfig.get().dateLimitAfter} and ${appConfig.get().dateLimitBefore}."
        val formattedPieChart = Template.pieChart(
            title = chartTitle,
            statsJsData = statsJsData
        )

        val pieChartFileName = FileUtil.authorPieChartFile(prAuthorId)
        val pieChartFile = File(pieChartFileName)
        pieChartFile.writeText(formattedPieChart)

        // Prepares data for bar chart generation
        // https://developers.google.com/chart/interactive/docs/gallery/barchart
        val barStatsJsData: String = listOf("['Reviewer', 'Total Reviewed', 'Total Commented']")
            .plus(
                stats.map {
                    "['${it.reviewerId}', ${it.totalReviews}, ${it.totalComments}]"
                }
            ).joinToString()

        val formattedBarChart = Template.barChart(
            title = chartTitle,
            chartData = barStatsJsData,
            dataSize = stats.size * 2 // Multiplied by data columns
        )
        val barChartFileName = FileUtil.authorBarChartFile(prAuthorId)
        val barChartFile = File(barChartFileName)
        barChartFile.writeText(formattedBarChart)

        return "📊 Written following charts for user: $prAuthorId. (Copy & paste file path URL in browser to preview)" +
            "\n - file://${pieChartFile.absolutePath}" +
            "\n - file://${barChartFile.absolutePath}"
    }

    /**
     * Formats [ReviewerReviewStats] that contains all review stats given by the reviewer.
     */
    override fun formatReviewerStats(stats: ReviewerReviewStats): String {
        if (stats.reviewedPrStats.isEmpty() || stats.reviewedForPrStats.isEmpty()) {
            return "⚠ ERROR: No reviewer stats to format. No charts to generate! ${Art.shrug}"
        }

        val headerItem: List<String> = listOf(
            "[" +
                "'Reviewed For different PR Authors', " +
                "'Total PRs Reviewed by ${stats.reviewerId} since ${appConfig.get().dateLimitAfter}', " +
                "'Total Source Code Review Comments', " +
                "'Total PR Issue Comments', " +
                "'Total PR Review Comments', " +
                "'Total All Comments Made'" +
                "]"
        )

        // Prepares data for bar chart generation
        // https://developers.google.com/chart/interactive/docs/gallery/barchart
        val barStatsJsData: String = headerItem
            .plus(
                stats.reviewedForPrStats.map { (prAuthorId, prReviewStats) ->
                    // Get all the comments made by the reviewer for the PR author
                    val userComments = prReviewStats.map { it.comments.values }.flatten()
                        .filter { it.user == stats.reviewerId }

                    "" +
                        "[" +
                        "'$prAuthorId', " +
                        "${prReviewStats.size}, " +
                        "${userComments.sumOf { it.codeReviewComment }}," +
                        "${userComments.sumOf { it.issueComment }}," +
                        "${userComments.sumOf { it.prReviewComment }}," +
                        "${userComments.sumOf { it.allComments }}" +
                        "]"
                }
            ).joinToString()

        val formattedBarChart = Template.barChart(
            title = "PRs Reviewed by ${stats.reviewerId}",
            chartData = barStatsJsData,
            dataSize = stats.reviewedForPrStats.size * 6 // Multiplied by data columns
        )
        val reviewedForBarChartFileName = FileUtil.prReviewedForCombinedBarChartFilename(stats.reviewerId)
        val reviewedForBarChartFile = File(reviewedForBarChartFileName)
        reviewedForBarChartFile.writeText(formattedBarChart)

        // Prepares data for bar chart generation
        // https://developers.google.com/chart/interactive/docs/gallery/barchart
        val userAllPrChartData: String = listOf(
            "" +
                "[" +
                "'PR#', " +
                "'Initial Response Time (mins)'," +
                "'Review Time (mins)'" +
                "]"
        ).plus(
            stats.reviewedPrStats.map { reviewStats ->
                "" +
                    "[" +
                    "'PR# ${reviewStats.pullRequest.number}', " +
                    "${reviewStats.initialResponseTime.toInt(DurationUnit.MINUTES)}," +
                    "${reviewStats.reviewCompletion.toInt(DurationUnit.MINUTES)}" +
                    "]"
            }
        ).joinToString()

        val appPrBarChart = Template.barChart(
            title = "PRs Reviewed by ${stats.reviewerId}",
            chartData = userAllPrChartData,
            dataSize = stats.reviewedPrStats.size
        )

        val allPrChartFileName = FileUtil.prReviewerReviewedPrStatsBarChartFile(stats.reviewerId)
        val allPrChartFile = File(allPrChartFileName)
        allPrChartFile.writeText(appPrBarChart)

        return "📊 Written following charts for user: ${stats.reviewerId}. (Copy & paste file path URL in browser to preview)" +
            "\n - file://${reviewedForBarChartFile.absolutePath}" +
            "\n - file://${allPrChartFile.absolutePath}"
    }
}
