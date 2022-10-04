package dev.hossain.githubstats.formatter

import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import dev.hossain.githubstats.AuthorReviewStats
import dev.hossain.githubstats.BuildConfig
import dev.hossain.githubstats.PrStats
import dev.hossain.githubstats.ReviewerReviewStats
import java.io.File
import kotlin.time.DurationUnit

class CsvFormatter : StatsFormatter {
    override fun formatPrStats(prStats: PrStats): String {
        return "Individual PR stats is not supported for CSV."
    }

    override fun formatAuthorStats(stats: List<AuthorReviewStats>): String {
        if (stats.isEmpty()) {
            return "⚠ ERROR: No stats to format. No CSV files for you! ¯\\_(ツ)_/¯"
        }

        // Create multiple CSV file per author for better visualization
        // Also create a single CSV with total reviews to visualize responsiveness to author
        val prAuthorId = stats.first().prAuthorId

        // Create report dir for the author
        val directory = File("REPORTS-$prAuthorId")
        if (directory.exists().not() && directory.mkdir()) {
            if (BuildConfig.DEBUG) {
                println("The reporting directory ${directory.path} created successfully.")
            }
        }

        // Write combine review count by reviewer
        val combinedReportHeaderRow = listOf(listOf("Reviewer", "Total PR Reviewed for $prAuthorId"))

        val combinedReportFileName = directory.path + File.separator + "REPORT_-_$prAuthorId-all-reviewers.csv"
        csvWriter().writeAll(combinedReportHeaderRow, combinedReportFileName)

        val filesCreated = mutableListOf<String>()
        stats.forEach { stat ->
            // Add a row for total reviews done by reviewer in the combined report
            csvWriter().writeAll(
                listOf(listOf(stat.reviewerId, stat.totalReviews)),
                combinedReportFileName,
                append = true
            )

            // Individual report per reviewer
            val fileName = generateCsvFileName(directory, stat)
            val headerItem: List<String> = listOf("Reviewer", "PR Number", "Review time (hours)")

            csvWriter().open(fileName) {
                writeRow(headerItem)

                stat.stats.forEach {
                    writeRow(
                        stat.reviewerId,
                        "PR ${it.pullRequest.number}",
                        "${it.reviewCompletion.toDouble(DurationUnit.HOURS)}"
                    )
                }
            }

            filesCreated.add(fileName)
        }
        return "Generated following files: \n${filesCreated.joinToString()} and $combinedReportFileName"
    }

    override fun formatReviewerStats(stats: ReviewerReviewStats): String {
        TODO("Not yet implemented")
    }

    private fun generateCsvFileName(directory: File, authorStats: AuthorReviewStats): String {
        return directory.path + File.separator + "REPORT-${authorStats.reviewerId}-for-${authorStats.prAuthorId}.csv"
    }
}
