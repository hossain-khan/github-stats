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
class HtmlChartFormatter: StatsFormatter, KoinComponent {
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

        val formattedChart = Template.pieChart(
            title = "PR reviewer`s stats for PRs created by `$prAuthorId` on `${appConfig.get().repoId}` repository " +
                    "between ${appConfig.get().dateLimitAfter} and ${appConfig.get().dateLimitBefore}.",
            statsJsData = statsJsData
        )

        val combinedReportFileName = FileUtil.authorChartFile(prAuthorId)
        File(combinedReportFileName).writeText(formattedChart)

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