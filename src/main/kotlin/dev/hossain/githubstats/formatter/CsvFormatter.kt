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

    /**
     * Formats PR review stats for list of users that reviewed specific user's PRs.
     */
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
            val headerItem: List<String> = listOf(
                "Reviewer",
                "PR Number",
                "Review time (mins)",
                "Code Review Comments",
                "PR Issue Comments",
                "PR Review Comments",
                "Total Comments",
                "PR URL"
            )

            csvWriter().open(fileName) {
                writeRow(headerItem)

                stat.stats.forEach { reviewStats ->
                    writeRow(
                        stat.reviewerId, /* "Reviewer" */
                        "PR ${reviewStats.pullRequest.number}", /* "PR Number" */
                        "${reviewStats.reviewCompletion.toInt(DurationUnit.MINUTES)}", /* "Review time (mins)" */
                        "${reviewStats.prComments.codeReviewComment}", /* "Code Review Comments" */
                        "${reviewStats.prComments.issueComment}", /* "PR Issue Comments" */
                        "${reviewStats.prComments.prReviewComment}", /* "PR Review Comments" */
                        "${reviewStats.prComments.allComments}", /* "Total Comments" */
                        reviewStats.pullRequest.html_url /* "PR URL" */
                    )
                }
            }

            filesCreated.add(fileName)
        }
        return "Generated following files: \n${filesCreated.joinToString()} and $combinedReportFileName"
    }

    /**
     * Formats [ReviewerReviewStats] that contains all review stats given by the reviewer.
     */
    override fun formatReviewerStats(stats: ReviewerReviewStats): String {
        if (stats.reviewedPrStats.isEmpty()) {
            return "⚠ ERROR: No stats to format. No CSV files for you! ${Art.shrug}"
        }

        // Generate two different CSV
        //  1. List of all the PRs reviewed
        //  2. List of author reviewed for

        val reviewedForFile = FileUtil.prReviewedForCombinedFilename(stats.reviewerId)
        val headerItem: List<String> = listOf(
            "Reviewed For different PR Authors",
            "Total PRs Reviewed by ${stats.reviewerId} since ${props.getDateLimit()}",
            "Total Code Review Comments",
            "Total PR Issue Comments",
            "Total PR Review Comments",
            "Total All Comments Made",
            "PR# List"
        )
        csvWriter().open(reviewedForFile) {
            writeRow(headerItem)

            stats.reviewedForPrStats.forEach { (prAuthorId, prReviewStats) ->
                val userComments = prReviewStats.map { it.comments.values }.flatten().filter { it.user == prAuthorId }
                writeRow(
                    prAuthorId,
                    prReviewStats.size,
                    userComments.sumOf { it.codeReviewComment },
                    userComments.sumOf { it.issueComment },
                    userComments.sumOf { it.prReviewComment },
                    userComments.sumOf { it.allComments },
                    prReviewStats.map { it.pullRequest.number }.sorted().toString()
                )
            }
        }

        val reviewerPrStatsFile = FileUtil.prReviewerReviewedPrStatsFile(stats.reviewerId)
        csvWriter().open(reviewerPrStatsFile) {
            writeRow(
                listOf(
                    "PR#",
                    "Review Time",
                    "Review Time (mins)",
                    "Code Review Comments",
                    "PR Issue Comments",
                    "PR Review Comments",
                    "Total Comments",
                    "PR Ready On",
                    "PR Merged On",
                    "Ready->Merge",
                    "PR Author",
                    "PR URL"
                )
            )
            stats.reviewedPrStats.forEach { reviewStats ->
                writeRow(
                    reviewStats.pullRequest.number.toString(),
                    reviewStats.reviewCompletion.toString(),
                    reviewStats.reviewCompletion.toInt(DurationUnit.MINUTES),
                    reviewStats.prComments.codeReviewComment.toString(),
                    reviewStats.prComments.issueComment.toString(),
                    reviewStats.prComments.prReviewComment.toString(),
                    reviewStats.prComments.allComments.toString(),
                    reviewStats.prReadyOn.toString(),
                    reviewStats.prMergedOn.toString(),
                    (reviewStats.prMergedOn - reviewStats.prReadyOn).toString(),
                    reviewStats.pullRequest.user.login,
                    reviewStats.pullRequest.html_url
                )
            }
        }

        return "Written '$reviewedForFile' and '$reviewerPrStatsFile'."
    }
}
