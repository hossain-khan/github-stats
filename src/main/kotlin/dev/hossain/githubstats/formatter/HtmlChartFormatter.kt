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

/**
 * Generates HTML based charts for the available data.
 * Currently, it uses [Google Chart](https://developers.google.com/chart) to generate simple charts.
 */
class HtmlChartFormatter : StatsFormatter, KoinComponent {
    private val appConfig: AppConfig by inject()

    override fun formatSinglePrStats(prStats: PrStats): String {
        val formattedChart = ""

        val prStatsFileName = FileUtil.prReportChart(prStats)
        File(prStatsFileName).writeText(formattedChart)

        return ""
    }

    override fun formatAuthorStats(stats: List<AuthorReviewStats>): String {
        if (stats.isEmpty()) {
            return "⚠ ERROR: No author stats to format. No charts to generate! ${Art.shrug}"
        }

        val prAuthorId = stats.first().prAuthorId

        // Prepares data for chart generation
        val statsJsData = stats.map {
            "['${it.reviewerId} [${it.stats.size}]', ${it.stats.size}]"
        }.joinToString()

        val formattedPieChart = Template.pieChart(
            title = "PR reviewer`s stats for PRs created by `$prAuthorId` on `${appConfig.get().repoId}` repository " +
                "between ${appConfig.get().dateLimitAfter} and ${appConfig.get().dateLimitBefore}.",
            statsJsData = statsJsData
        )

        val pieChartFileName = FileUtil.authorPieChartFile(prAuthorId)
        File(pieChartFileName).writeText(formattedPieChart)


        /*
          ['Reviewer', 'Sales', 'Expenses', 'Profit'],
          ['2014', 1000, 400, 200],
          ['2015', 1170, 460, 250],
          ['2016', 660, 1120, 300],
          ['2017', 1030, 540, 350]
         */
        val barStatsJsData: String = listOf("['Reviewer', 'Total Reviewed', 'Total Commented']")
            .plus(stats.map {
                "['${it.reviewerId}', ${it.totalReviews}, ${it.stats.map { it.prComments }.sumOf { it.allComments }}]"
            }).joinToString()

        val formattedBarChart = Template.barChart(
            title = "Title",
            chartData = barStatsJsData
        )
        val barChartFileName = FileUtil.authorBarChartFile(prAuthorId)
        File(barChartFileName).writeText(formattedBarChart)

        return ""
    }

    override fun formatReviewerStats(stats: ReviewerReviewStats): String {
        if (stats.reviewedPrStats.isEmpty() || stats.reviewedForPrStats.isEmpty()) {
            return "⚠ ERROR: No reviewer stats to format. No charts to generate! ${Art.shrug}"
        }
        val formattedChart = ""

        val combinedReportFileName = FileUtil.reviewerReportFile(stats.reviewerId)
        File(combinedReportFileName).writeText(formattedChart)

        return ""
    }
}