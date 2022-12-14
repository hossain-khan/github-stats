package dev.hossain.githubstats.formatter

import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import dev.hossain.ascii.Art
import dev.hossain.githubstats.AuthorReviewStats
import dev.hossain.githubstats.PrStats
import dev.hossain.githubstats.ReviewStats
import dev.hossain.githubstats.ReviewerReviewStats
import dev.hossain.githubstats.util.FileUtil
import dev.hossain.githubstats.util.LocalProperties
import dev.hossain.time.toWorkingHour
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
        val combinedReportHeaderRow = listOf(listOf("Reviewer", "Total PR Reviewed for $prAuthorId since ${props.getDateLimitAfter()}"))

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
                "Initial Response time (mins)",
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
                        "${reviewStats.initialResponseTime.toInt(DurationUnit.MINUTES)}", /* "Initial Response time (mins)" */
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
            "Total PRs Reviewed by ${stats.reviewerId} since ${props.getDateLimitAfter()}",
            "Total Code Review Comments",
            "Total PR Issue Comments",
            "Total PR Review Comments",
            "Total All Comments Made",
            "PR# List"
        )
        csvWriter().open(reviewedForFile) {
            writeRow(headerItem)

            stats.reviewedForPrStats.forEach { (prAuthorId, prReviewStats) ->
                // Get all the comments made by the reviewer for the PR author
                val userComments = prReviewStats.map { it.comments.values }.flatten()
                    .filter { it.user == stats.reviewerId }
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
                    "Review Time (working days)",
                    "Review Time (mins)",
                    "Initial Response Time (working days)",
                    "Initial Response Time (mins)",
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
            stats.reviewedPrStats.forEach { reviewStats: ReviewStats ->
                writeRow(
                    reviewStats.pullRequest.number,
                    reviewStats.reviewCompletion,
                    reviewStats.reviewCompletion.toWorkingHour(),
                    reviewStats.reviewCompletion.toInt(DurationUnit.MINUTES),
                    reviewStats.initialResponseTime.toWorkingHour(),
                    reviewStats.initialResponseTime.toInt(DurationUnit.MINUTES),
                    reviewStats.prComments.codeReviewComment,
                    reviewStats.prComments.issueComment,
                    reviewStats.prComments.prReviewComment,
                    reviewStats.prComments.allComments,
                    reviewStats.prReadyOn,
                    reviewStats.prMergedOn,
                    (reviewStats.prMergedOn - reviewStats.prReadyOn),
                    reviewStats.pullRequest.user.login,
                    reviewStats.pullRequest.html_url
                )
            }
        }

        return "Written '$reviewedForFile' and '$reviewerPrStatsFile'."
    }
}
