package dev.hossain.githubstats.formatter

import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import dev.hossain.githubstats.AuthorReviewStats
import dev.hossain.githubstats.PrStats
import kotlin.time.DurationUnit

class CsvFormatter : StatsFormatter {
    override fun formatPrStats(prStats: PrStats): String {
        return "Individual PR stats is not supported for CSV."
    }

    override fun formatAuthorStats(stats: List<AuthorReviewStats>): String {
        // Create multiple CSV file per author for better visualization
        // Also create a single CSV with total reviews to visualize responsiveness to author

        val filesCreated = mutableListOf<String>()
        stats.forEach { stat ->
            val fileName = generateCsvFileName(stat)
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

        // Write combine review count by reviewer
        val headerRow = listOf(listOf("Reviewer", "Total PR Reviewed"))
        val combinedReportFileName = "REPORT-${stats.first().prAuthorId}-all-reviewers.csv"
        csvWriter().writeAll(headerRow, combinedReportFileName)
        stats.forEach { stat ->
            csvWriter().writeAll(
                listOf(listOf(stat.reviewerId, stat.totalReviews)),
                combinedReportFileName,
                append = true
            )
        }

        return "Generated following files: \n${filesCreated.joinToString()} and $combinedReportFileName"
    }

    private fun generateCsvFileName(authorStats: AuthorReviewStats): String {
        return "REPORT-${authorStats.reviewerId}-for-${authorStats.prAuthorId}.csv"
    }
}
