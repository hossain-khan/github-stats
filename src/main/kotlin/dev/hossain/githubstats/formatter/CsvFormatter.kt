package dev.hossain.githubstats.formatter

import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import dev.hossain.githubstats.AuthorReviewStats
import dev.hossain.githubstats.PrStats
import dev.hossain.githubstats.ReviewerReviewStats
import dev.hossain.githubstats.util.FileUtil
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
        val directory: File = FileUtil.createReportDir(prAuthorId)

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
        // Generate two different CSV
        //  1. List of all the PRs reviewed
        //  2. List of author reviewed for

        val reviewedForFile = FileUtil.prReviewedForCombinedFilename(stats.reviewerId)
        val headerItem: List<String> = listOf("Reviewed For", "Total PRs Reviewed", "PR# List")
        csvWriter().open(reviewedForFile) {
            writeRow(headerItem)

            stats.reviewedForPrStats.forEach { (prAuthorId, prReviewStats) ->
                writeRow(
                    prAuthorId,
                    prReviewStats.size,
                    prReviewStats.map { it.pullRequest.number }.sorted().toString()
                )
            }
        }

        val reviewerPrStatsFile = FileUtil.prReviewerReviewedPrStatsFile(stats.reviewerId)
        csvWriter().open(reviewerPrStatsFile) {
            writeRow(listOf("PR#", "Review Time", "PR Ready On", "PR Merged On", "Ready->Merge", "PR Author", "PR URL"))
            stats.reviewedPrStats.forEach {
                writeRow(
                    it.pullRequest.number.toString(),
                    it.reviewCompletion.toString(),
                    it.prReadyOn.toString(),
                    it.prMergedOn.toString(),
                    (it.prMergedOn - it.prReadyOn).toString(),
                    it.pullRequest.user.login,
                    it.pullRequest.html_url
                )
            }
        }

        return "Written '$reviewedForFile' and '$reviewerPrStatsFile'."
    }

    private fun generateCsvFileName(directory: File, authorStats: AuthorReviewStats): String {
        return directory.path + File.separator + "REPORT-${authorStats.reviewerId}-for-${authorStats.prAuthorId}.csv"
    }
}
