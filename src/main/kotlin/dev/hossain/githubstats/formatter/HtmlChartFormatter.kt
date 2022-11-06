package dev.hossain.githubstats.formatter

import dev.hossain.ascii.Art
import dev.hossain.githubstats.AuthorReviewStats
import dev.hossain.githubstats.PrStats
import dev.hossain.githubstats.ReviewerReviewStats
import dev.hossain.githubstats.formatter.html.Template
import dev.hossain.githubstats.util.FileUtil
import java.io.File

/**
 * Generates HTML based charts for the available data.
 * Currently, it uses [Google Chart](https://developers.google.com/chart) to generate simple charts.
 */
class HtmlChartFormatter: StatsFormatter {
    override fun formatSinglePrStats(prStats: PrStats): String {
        val formattedPrStats = ""

        val prStatsFileName = FileUtil.prReportChart(prStats)
        File(prStatsFileName).writeText(formattedPrStats)

        return ""
    }

    override fun formatAuthorStats(stats: List<AuthorReviewStats>): String {
        if (stats.isEmpty()) {
            return "⚠ ERROR: No author stats to format. No files to write! ${Art.shrug}"
        }

        val prAuthorId = stats.first().prAuthorId

        val statsJsData = stats.map {
            "['${it.reviewerId}', ${it.stats.size}]"
        }.joinToString()

        val formattedChart = Template.pieChart(prAuthorId, statsJsData)

        val combinedReportFileName = FileUtil.authorChartFile(prAuthorId)
        File(combinedReportFileName).writeText(formattedChart)

        return ""
    }

    override fun formatReviewerStats(stats: ReviewerReviewStats): String {
        if (stats.reviewedPrStats.isEmpty() || stats.reviewedForPrStats.isEmpty()) {
            return "⚠ ERROR: No reviewer stats to format. No files to write! ${Art.shrug}"
        }
        val formattedStats = ""

        val combinedReportFileName = FileUtil.reviewerReportFile(stats.reviewerId)
        File(combinedReportFileName).writeText(formattedStats)

        return ""
    }
}