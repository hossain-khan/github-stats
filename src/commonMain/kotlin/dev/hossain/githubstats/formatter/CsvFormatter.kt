package dev.hossain.githubstats.formatter

import dev.hossain.ascii.Art
import dev.hossain.githubstats.AuthorPrStats
import dev.hossain.githubstats.AuthorStats
import dev.hossain.githubstats.PrStats
import dev.hossain.githubstats.ReviewStats
import dev.hossain.githubstats.ReviewerReviewStats
import dev.hossain.githubstats.util.AppConfig
import dev.hossain.githubstats.util.FileUtil // FileUtil will need refactoring to use PlatformFile
import dev.hossain.platform.ExpectedPropertiesReader
import dev.hossain.platform.PlatformCsvWriter
import dev.hossain.platform.getCsvWriter
import dev.hossain.time.toWorkingHour
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.DurationUnit

class CsvFormatter :
    StatsFormatter,
    KoinComponent {
    private val props: ExpectedPropertiesReader by inject()
    private val appConfig: AppConfig by inject()
    private val csvWriter: PlatformCsvWriter = getCsvWriter()

    override fun formatSinglePrStats(prStats: PrStats): String = "Individual PR stats is not supported for CSV export."

    override fun formatAuthorStats(stats: AuthorStats): String {
        if (stats.reviewStats.isEmpty()) {
            return "⚠ ERROR: No stats to format. No CSV files for you! ${Art.SHRUG}"
        }

        val prAuthorId = stats.reviewStats.first().prAuthorId
        val dateLimitAfter = props.getDateLimitAfter() ?: "an unspecified date"

        val combinedReportHeaderRow = listOf(listOf("Reviewer", "Total PR Reviewed for $prAuthorId since $dateLimitAfter"))
        // Assuming FileUtil.allReviewersForAuthorFile returns a String path compatible with PlatformCsvWriter
        val combinedReportFileName = FileUtil.allReviewersForAuthorFile(prAuthorId)
        csvWriter.writeAll(combinedReportHeaderRow, combinedReportFileName)

        val filesCreated = mutableListOf<String>()
        stats.reviewStats.forEach { stat ->
            csvWriter.writeAll(
                listOf(listOf(stat.reviewerId, stat.totalReviews)),
                combinedReportFileName,
                append = true,
            )

            val fileName = FileUtil.reviewedForAuthorCsvFile(stat)
            val headerItem: List<String> =
                listOf(
                    "Reviewer",
                    "PR Number",
                    "Review time (mins)",
                    "Initial Response time (mins)",
                    "Code Review Comments",
                    "PR Issue Comments",
                    "PR Review Comments",
                    "Total Comments",
                    "PR URL",
                )

            csvWriter.open(fileName) { // PlatformCsvWriterSession is the receiver here
                writeRow(headerItem)
                stat.stats.forEach { reviewStats ->
                    writeRow(
                        stat.reviewerId,
                        "PR ${reviewStats.pullRequest.number}",
                        "${reviewStats.reviewCompletion.toInt(DurationUnit.MINUTES)}",
                        "${reviewStats.initialResponseTime.toInt(DurationUnit.MINUTES)}",
                        "${reviewStats.prComments.codeReviewComment}",
                        "${reviewStats.prComments.issueComment}",
                        "${reviewStats.prComments.prReviewSubmissionComment}",
                        "${reviewStats.prComments.allComments}",
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
        csvWriter.open(targetFileName) {
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

    override fun formatReviewerStats(stats: ReviewerReviewStats): String {
        if (stats.reviewedPrStats.isEmpty()) {
            return "⚠ ERROR: No stats to format. No CSV files for you! ${Art.SHRUG}"
        }

        val dateLimitAfter = props.getDateLimitAfter() ?: "an unspecified date"
        val reviewedForFile = FileUtil.prReviewedForCombinedFilename(stats.reviewerId)
        val headerItem: List<String> =
            listOf(
                "Reviewed For different PR Authors",
                "Total PRs Reviewed by ${stats.reviewerId} since $dateLimitAfter",
                "Total Code Review Comments",
                "Total PR Issue Comments",
                "Total PR Review Comments",
                "Total All Comments Made",
                "PR# List",
            )
        csvWriter.open(reviewedForFile) {
            writeRow(headerItem)
            stats.reviewedForPrStats.forEach { (prAuthorId, prReviewStats) ->
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
        csvWriter.open(reviewerPrStatsFile) {
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
                    (reviewStats.prMergedOn - reviewStats.prReadyOn), // This will require Duration to be KMP compatible or expect/actual for subtraction
                    reviewStats.pullRequest.user.login,
                    reviewStats.pullRequest.html_url,
                )
            }
        }
        return "Written '$reviewedForFile' and '$reviewerPrStatsFile'."
    }
}
