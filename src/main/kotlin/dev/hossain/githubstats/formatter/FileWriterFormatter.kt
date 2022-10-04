package dev.hossain.githubstats.formatter

import dev.hossain.githubstats.AuthorReviewStats
import dev.hossain.githubstats.PrStats
import dev.hossain.githubstats.ReviewerReviewStats
import dev.hossain.githubstats.util.FileUtil
import java.io.File

/**
 * Writes report from given [formatter] into file.
 */
class FileWriterFormatter constructor(
    private val formatter: StatsFormatter
) : StatsFormatter {
    override fun formatPrStats(prStats: PrStats): String {
        val formattedPrStats = formatter.formatPrStats(prStats)

        val prStatsFileName = FileUtil.prReportFile(prStats)
        File(prStatsFileName).writeText(formattedPrStats)

        return ""
    }

    override fun formatAuthorStats(stats: List<AuthorReviewStats>): String {
        if (stats.isEmpty()) {
            return "⚠ ERROR: No stats to format. No files to write! ¯\\_(ツ)_/¯"
        }

        // Create multiple CSV file per author for better visualization
        // Also create a single CSV with total reviews to visualize responsiveness to author
        val prAuthorId = stats.first().prAuthorId

        val formattedAuthorStats = formatter.formatAuthorStats(stats)

        val combinedReportFileName = FileUtil.authorReportFile(prAuthorId)
        File(combinedReportFileName).writeText(formattedAuthorStats)

        return ""
    }

    override fun formatReviewerStats(stats: ReviewerReviewStats): String {
        return ""
    }
}
