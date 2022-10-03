package dev.hossain.githubstats.formatter

import dev.hossain.githubstats.AuthorReviewStats
import dev.hossain.githubstats.BuildConfig
import dev.hossain.githubstats.PrStats
import java.io.File

/**
 * Writes report from given [formatter] into file.
 */
class FileWriterFormatter constructor(
    private val formatter: StatsFormatter
) : StatsFormatter {
    override fun formatPrStats(prStats: PrStats): String {
        val formattedPrStats = formatter.formatPrStats(prStats)

        // Create report dir for the author
        val directory = createReportDir("PRs")

        val combinedReportFileName = directory.path + File.separator + "REPORT-PR-${prStats.pullRequest.number}.txt"
        File(combinedReportFileName).writeText(formattedPrStats)

        return ""
    }

    override fun formatAuthorStats(stats: List<AuthorReviewStats>): String {
        if (stats.isEmpty()) {
            return "⚠ ERROR: No stats to format. No CSV files for you! ¯\\_(ツ)_/¯"
        }

        // Create multiple CSV file per author for better visualization
        // Also create a single CSV with total reviews to visualize responsiveness to author
        val prAuthorId = stats.first().prAuthorId

        // Create report dir for the author
        val directory = createReportDir(prAuthorId)

        val formattedAuthorStats = formatter.formatAuthorStats(stats)

        val combinedReportFileName = directory.path + File.separator + "REPORT_-_$prAuthorId.txt"
        File(combinedReportFileName).writeText(formattedAuthorStats)

        return ""
    }

    private fun createReportDir(directoryName: String): File {
        val directory = File("REPORTS-$directoryName")
        if (directory.exists().not() && directory.mkdir()) {
            if (BuildConfig.DEBUG) {
                println("The reporting directory ${directory.path} created successfully.")
            }
        }
        return directory
    }
}
