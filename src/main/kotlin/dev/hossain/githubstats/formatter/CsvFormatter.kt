package dev.hossain.githubstats.formatter

import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import dev.hossain.ascii.Art
import dev.hossain.githubstats.AuthorPrStats
import dev.hossain.githubstats.AuthorStats
import dev.hossain.githubstats.PrStats
import dev.hossain.githubstats.ReviewStats
import dev.hossain.githubstats.ReviewerReviewStats
import dev.hossain.githubstats.util.AppConfig
import dev.hossain.githubstats.util.FileUtil
import dev.hossain.githubstats.util.LocalProperties
import dev.hossain.i18n.Resources
import dev.hossain.time.toWorkingHour
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.DurationUnit

class CsvFormatter :
    StatsFormatter,
    KoinComponent {
    private val resources: Resources by inject()
    private val props: LocalProperties by inject()
    private val appConfig: AppConfig by inject()

    override fun formatSinglePrStats(prStats: PrStats): String = resources.string("csv_not_supported")

    /**
     * Formats PR review stats for list of users that reviewed specific user's PRs.
     */
    override fun formatAuthorStats(stats: AuthorStats): String {
        if (stats.reviewStats.isEmpty()) {
            return resources.string("csv_no_stats_error", Art.SHRUG)
        }

        // Create multiple CSV file per author for better visualization
        // Also create a single CSV with total reviews to visualize responsiveness to author
        val prAuthorId = stats.reviewStats.first().prAuthorId

        // Write combine review count by reviewer
        val combinedReportHeaderRow =
            listOf(
                listOf(
                    resources.string("csv_header_reviewer"),
                    resources.string(
                        "csv_header_total_pr_reviewed",
                        prAuthorId,
                        props.getDateLimitAfter(),
                    ),
                ),
            )

        val combinedReportFileName = FileUtil.allReviewersForAuthorFile(prAuthorId)
        csvWriter().writeAll(combinedReportHeaderRow, combinedReportFileName)

        val filesCreated = mutableListOf<String>()
        stats.reviewStats.forEach { stat ->
            // Add a row for total reviews done by reviewer in the combined report
            csvWriter().writeAll(
                listOf(listOf(stat.reviewerId, stat.totalReviews)),
                combinedReportFileName,
                append = true,
            )

            // Individual report per reviewer
            val fileName = FileUtil.reviewedForAuthorCsvFile(stat)
            val headerItem: List<String> =
                listOf(
                    resources.string("csv_header_reviewer"),
                    resources.string("csv_header_pr_number"),
                    resources.string("csv_header_review_time_mins"),
                    resources.string("csv_header_initial_response_time_mins"),
                    resources.string("csv_header_code_review_comments"),
                    resources.string("csv_header_pr_issue_comments"),
                    resources.string("csv_header_pr_review_comments"),
                    resources.string("csv_header_total_comments"),
                    resources.string("csv_header_pr_url"),
                )

            csvWriter().open(fileName) {
                writeRow(headerItem)

                stat.stats.forEach { reviewStats ->
                    writeRow(
                        // "Reviewer"
                        stat.reviewerId,
                        // "PR Number"
                        resources.string("csv_pr_number_format", reviewStats.pullRequest.number),
                        // "Review time (mins)"
                        "${reviewStats.reviewCompletion.toInt(DurationUnit.MINUTES)}",
                        // "Initial Response time (mins)"
                        "${reviewStats.initialResponseTime.toInt(DurationUnit.MINUTES)}",
                        // "Code Review Comments"
                        "${reviewStats.prComments.codeReviewComment}",
                        // "PR Issue Comments"
                        "${reviewStats.prComments.issueComment}",
                        // "PR Review Comments"
                        "${reviewStats.prComments.prReviewSubmissionComment}",
                        // "Total Comments"
                        "${reviewStats.prComments.allComments}",
                        // "PR URL"
                        reviewStats.pullRequest.html_url,
                    )
                }
            }

            filesCreated.add(fileName)
        }
        return "Generated following files: \n${filesCreated.joinToString()} and $combinedReportFileName"
    }

    override fun formatAllAuthorStats(aggregatedPrStats: List<AuthorPrStats>): String {
        if (aggregatedPrStats.isEmpty()) {
            return "⚠ ERROR: No aggregated stats to format. No CSV files for you! ${Art.SHRUG}"
        }

        // Generate aggregated PR review stats
        //  1. List of users that created PR and cumulative stats about those PRs

        val targetFileName = FileUtil.repositoryAggregatedPrStatsByAuthorFilename()
        val headerItem: List<String> =
            listOf(
                "Stats Date Range",
                "PR Author ID (created by)",
                "Total PRs Created by Author",
                "Total Source Code Review Comments",
                "Total PR Issue Comments (not associated with code)",
                "Total PR Review Submission comments (reviewed or request change)",
            )
        csvWriter().open(targetFileName) {
            writeRow(headerItem)

            aggregatedPrStats.filter { it.isEmpty().not() }.forEach {
                writeRow(
                    "Between ${appConfig.get().dateLimitAfter} and ${appConfig.get().dateLimitBefore}",
                    it.authorUserId,
                    it.totalPrsCreated,
                    it.totalCodeReviewComments,
                    it.totalIssueComments,
                    it.totalPrSubmissionComments,
                )
            }
        }

        return "Generated following files: \n$targetFileName"
    }

    /**
     * Formats [ReviewerReviewStats] that contains all review stats given by the reviewer.
     */
    override fun formatReviewerStats(stats: ReviewerReviewStats): String {
        if (stats.reviewedPrStats.isEmpty()) {
            return "⚠ ERROR: No stats to format. No CSV files for you! ${Art.SHRUG}"
        }

        // Generate two different CSV
        //  1. List of all the PRs reviewed
        //  2. List of author reviewed for

        val reviewedForFile = FileUtil.prReviewedForCombinedFilename(stats.reviewerId)
        val headerItem: List<String> =
            listOf(
                "Reviewed For different PR Authors",
                "Total PRs Reviewed by ${stats.reviewerId} since ${props.getDateLimitAfter()}",
                "Total Code Review Comments",
                "Total PR Issue Comments",
                "Total PR Review Comments",
                "Total All Comments Made",
                "PR# List",
            )
        csvWriter().open(reviewedForFile) {
            writeRow(headerItem)

            stats.reviewedForPrStats.forEach { (prAuthorId, prReviewStats) ->
                // Get all the comments made by the reviewer for the PR author
                val userComments =
                    prReviewStats
                        .map { it.comments.values }
                        .flatten()
                        .filter { it.user == stats.reviewerId }
                writeRow(
                    prAuthorId,
                    prReviewStats.size,
                    userComments.sumOf { it.codeReviewComment },
                    userComments.sumOf { it.issueComment },
                    userComments.sumOf { it.prReviewSubmissionComment },
                    userComments.sumOf { it.allComments },
                    prReviewStats.map { it.pullRequest.number }.sorted().toString(),
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
                    "PR URL",
                ),
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
                    reviewStats.prComments.prReviewSubmissionComment,
                    reviewStats.prComments.allComments,
                    reviewStats.prReadyOn,
                    reviewStats.prMergedOn,
                    (reviewStats.prMergedOn - reviewStats.prReadyOn),
                    reviewStats.pullRequest.user.login,
                    reviewStats.pullRequest.html_url,
                )
            }
        }

        return "Written '$reviewedForFile' and '$reviewerPrStatsFile'."
    }
}
