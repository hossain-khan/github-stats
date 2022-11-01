package dev.hossain.githubstats.formatter

import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import dev.hossain.ascii.Art
import dev.hossain.githubstats.AuthorReviewStats
import dev.hossain.githubstats.PrStats
import dev.hossain.githubstats.ReviewerReviewStats
import dev.hossain.githubstats.util.FileUtil
import dev.hossain.githubstats.util.LocalProperties
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.DurationUnit

class CsvFormatter : StatsFormatter, KoinComponent {
    private val props: LocalProperties by inject()
    override fun formatSinglePrStats(prStats: PrStats): String {
        return "Individual PR stats is not supported for CSV export."
    }

    override fun formatAuthorStats(stats: List<AuthorReviewStats>): String {
        if (stats.isEmpty()) {
            return "⚠ ERROR: No stats to format. No CSV files for you! ${Art.shrug}"
        }

        // Create multiple CSV file per author for better visualization
        // Also create a single CSV with total reviews to visualize responsiveness to author
        val prAuthorId = stats.first().prAuthorId

        // Write combine review count by reviewer
        val combinedReportHeaderRow = listOf(listOf("Reviewer", "Total PR Reviewed for $prAuthorId since ${props.getDateLimit()}"))

        val combinedReportFileName = FileUtil.allReviewersForAuthorFile(prAuthorId)
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
            val fileName = FileUtil.reviewedForAuthorFileName(stat)
            val headerItem: List<String> = listOf("Reviewer", "PR Number", "Review time (mins)", "URL")

            csvWriter().open(fileName) {
                writeRow(headerItem)

                stat.stats.forEach {
                    writeRow(
                        stat.reviewerId,
                        "PR ${it.pullRequest.number}",
                        "${it.reviewCompletion.toInt(DurationUnit.MINUTES)}",
                        it.pullRequest.html_url
                    )
                }
            }

            filesCreated.add(fileName)
        }
        return "Generated following files: \n${filesCreated.joinToString()} and $combinedReportFileName"
    }

    override fun formatReviewerStats(stats: ReviewerReviewStats): String {
        if (stats.reviewedPrStats.isEmpty()) {
            return "⚠ ERROR: No stats to format. No CSV files for you! ${Art.shrug}"
        }

        // Generate two different CSV
        //  1. List of all the PRs reviewed
        //  2. List of author reviewed for

        val reviewedForFile = FileUtil.prReviewedForCombinedFilename(stats.reviewerId)
        val headerItem: List<String> = listOf("Reviewed For different PR Authors", "Total PRs Reviewed by ${stats.reviewerId} since ${props.getDateLimit()}", "PR# List")
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
            writeRow(listOf("PR#", "Review Time", "Review Time (mins)", "PR Ready On", "PR Merged On", "Ready->Merge", "PR Author", "PR URL"))
            stats.reviewedPrStats.forEach {
                writeRow(
                    it.pullRequest.number.toString(),
                    it.reviewCompletion.toString(),
                    it.reviewCompletion.toInt(DurationUnit.MINUTES),
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
}
