package dev.hossain.githubstats.formatter

import dev.hossain.ascii.Art
import dev.hossain.githubstats.AuthorStats
import dev.hossain.githubstats.PrStats
import dev.hossain.githubstats.ReviewerReviewStats
import dev.hossain.githubstats.util.FileUtil
import java.io.File

/**
 * Writes report from given [formatter] into file.
 * The main purpose is to use the [PicnicTableFormatter] and write the output to file for later reference.
 */
class FileWriterFormatter constructor(
    private val formatter: StatsFormatter
) : StatsFormatter {
    override fun formatSinglePrStats(prStats: PrStats): String {
        val formattedPrStats = formatter.formatSinglePrStats(prStats)

        val prStatsFileName = FileUtil.prReportFile(prStats)
        File(prStatsFileName).writeText(formattedPrStats)

        return ""
    }

    override fun formatAuthorStats(stats: AuthorStats): String {
        if (stats.reviewStats.isEmpty()) {
            return "⚠ ERROR: No author stats to format. No files to write! ${Art.shrug}"
        }

        // Create multiple CSV file per author for better visualization
        // Also create a single CSV with total reviews to visualize responsiveness to author
        val prAuthorId = stats.reviewStats.first().prAuthorId

        val formattedStats = formatter.formatAuthorStats(stats)

        val combinedReportFileName = FileUtil.authorReportFile(prAuthorId)
        File(combinedReportFileName).writeText(formattedStats)

        return ""
    }

    override fun formatReviewerStats(stats: ReviewerReviewStats): String {
        if (stats.reviewedPrStats.isEmpty() || stats.reviewedForPrStats.isEmpty()) {
            return "⚠ ERROR: No reviewer stats to format. No files to write! ${Art.shrug}"
        }
        val formattedStats = formatter.formatReviewerStats(stats)

        val combinedReportFileName = FileUtil.reviewerReportFile(stats.reviewerId)
        File(combinedReportFileName).writeText(formattedStats)

        return ""
    }
}
