package dev.hossain.githubstats.formatter

import com.jakewharton.picnic.TextAlignment
import com.jakewharton.picnic.table
import dev.hossain.ascii.Art
import dev.hossain.githubstats.AuthorPrStats
import dev.hossain.githubstats.AuthorStats
import dev.hossain.githubstats.PrStats
import dev.hossain.githubstats.ReviewStats
import dev.hossain.githubstats.ReviewerReviewStats
import dev.hossain.githubstats.UserId
import dev.hossain.githubstats.UserPrComment
import dev.hossain.githubstats.avgMergeTime
import dev.hossain.githubstats.util.AppConfig
import dev.hossain.time.toWorkingHour // This was a commonMain extension, assuming it's KMP compatible or adapted
import kotlinx.datetime.Instant // kotlinx-datetime Instant used in common models
import kotlinx.datetime.toJavaInstant // KMP utility to convert to java.time.Instant for JVM
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import kotlin.time.Duration

/**
 * Actual JVM implementation for formatting stats using Picnic library.
 */
actual class PicnicTableFormatter actual constructor() : StatsFormatter, KoinComponent {
    private val dateFormatter =
        DateTimeFormatter
            .ofLocalizedDateTime(FormatStyle.MEDIUM)
            .withLocale(Locale.US)
            .withZone(ZoneId.systemDefault()) // System default is JVM specific behavior

    private val appConfig: AppConfig by inject()

    override actual fun formatSinglePrStats(prStats: PrStats): String {
        fun formatUserPrComments(userPrComment: UserPrComment) =
            "${userPrComment.user} made total ${userPrComment.allComments} ${userPrComment.allComments.comments()}.\n" +
                "Code Review Comments = ${userPrComment.codeReviewComment}, " +
                "Issue Comments = ${userPrComment.issueComment}" +
                if (userPrComment.prReviewSubmissionComment > 0) {
                    "\nHas reviewed PR ${userPrComment.prReviewSubmissionComment} ${userPrComment.prReviewSubmissionComment.times()}."
                } else {
                    ""
                }

        fun formatUserDuration(userDuration: Map.Entry<UserId, Duration>): String =
            // Assuming toWorkingHour() is a KMP compatible extension or will be adapted
            "$userDuration | ${userDuration.value.toWorkingHour()}"


        return table {
            cellStyle {
                border = true
                paddingLeft = 1
                paddingRight = 1
            }
            row("Title", prStats.pullRequest.title)
            row("PR Author", prStats.pullRequest.user.login)
            row("URL", prStats.pullRequest.html_url)
            row("Ready On", dateFormatter.format(prStats.prReadyOn.toJavaInstant()))
            if (prStats.initialResponseTime.isNotEmpty()) {
                row {
                    cell("PR Initial Response Time") {
                        rowSpan = prStats.initialResponseTime.size
                    }
                    cell(formatUserDuration(prStats.initialResponseTime.entries.first()))
                }
                prStats.initialResponseTime.entries.drop(1).forEach {
                    row(formatUserDuration(it))
                }
            }
            if (prStats.prApprovalTime.isNotEmpty()) {
                row {
                    cell("PR Approval Review Time") {
                        rowSpan = prStats.prApprovalTime.size
                    }
                    cell(formatUserDuration(prStats.prApprovalTime.entries.first()))
                }
                prStats.prApprovalTime.entries.drop(1).forEach {
                    row(formatUserDuration(it))
                }
            }
            if (prStats.comments.isNotEmpty()) {
                row {
                    cell("PR Comments") {
                        rowSpan = prStats.comments.size
                    }
                    cell(
                        formatUserPrComments(
                            prStats.comments.entries
                                .first()
                                .value,
                        ),
                    )
                }
                prStats.comments.entries.drop(1).forEach {
                    row(formatUserPrComments(it.value))
                }
            }
            row("Merged On", dateFormatter.format(prStats.prMergedOn.toJavaInstant()))
            // Duration subtraction is KMP compatible
            row("Open → Merge", "${prStats.prMergedOn - prStats.prReadyOn}")
        }.toString()
    }

    override actual fun formatAuthorStats(stats: AuthorStats): String {
        if (stats.reviewStats.isEmpty()) {
            return "⚠ ERROR: No stats to format. No ◫ fancy tables for you! ${Art.SHRUG}"
        }

        @Suppress("ktlint:standard:max-line-length")
        fun formatPrReviewTimeAndComments(reviewStats: ReviewStats): String =
            "${reviewStats.reviewCompletion} for PR#${reviewStats.pullRequest.number}" +
                if (reviewStats.prComments.isEmpty().not()) {
                    "\nmade ${reviewStats.prComments.codeReviewComment} code review ${reviewStats.prComments.codeReviewComment.comments()} " +
                        "and ${reviewStats.prComments.issueComment} issue ${reviewStats.prComments.issueComment.comments()}."
                } else {
                    ""
                } +
                if (reviewStats.prComments.prReviewSubmissionComment > 0) {
                    "\nalso has reviewed PR ${reviewStats.prComments.prReviewSubmissionComment} ${reviewStats.prComments.prReviewSubmissionComment.times()}."
                } else {
                    ""
                }

        val repoId = stats.reviewStats.first().repoId
        val prAuthorId = stats.reviewStats.first().prAuthorId

        return table {
            cellStyle {
                border = true
                alignment = TextAlignment.TopLeft
                paddingLeft = 1
                paddingRight = 1
            }

            header {
                cellStyle {
                    border = false
                    alignment = TextAlignment.TopCenter
                    paddingBottom = 1
                    paddingTop = 2
                }
                row {
                    val headingText =
                        "PR reviewer's stats for PRs created by '$prAuthorId' on '$repoId' repository " +
                            "between ${appConfig.get().dateLimitAfter} and ${appConfig.get().dateLimitBefore}."
                    val headingSeparator = "-".repeat(headingText.length)
                    cell("$headingSeparator\n$headingText\n$headingSeparator") {
                        columnSpan = 2
                    }
                }
            }

            stats.reviewStats.forEach { stat ->
                row {
                    cellStyle {
                        paddingTop = 1
                    }
                    cell("● PR reviewer stats for \"${stat.reviewerId}\"") {
                        columnSpan = 2
                    }
                }
                row("Total Reviews", "${stat.totalReviews}")
                row {
                    cell("Review Durations") {
                        rowSpan = stat.stats.size
                    }
                    cell(formatPrReviewTimeAndComments(stat.stats.first()))
                }
                stat.stats.drop(1).forEach {
                    row(formatPrReviewTimeAndComments(it))
                }
                row("Average Time", "${stat.average}")
            }

            row {
                cellStyle {
                    paddingTop = 2
                }
                cell("● Average PR Merge Time for all PRs created by '$prAuthorId'")
                // Assuming avgMergeTime() is KMP compatible or will be adapted
                cell("${stats.reviewStats.avgMergeTime()}")
            }
        }.toString()
    }

    override actual fun formatAllAuthorStats(aggregatedPrStats: List<AuthorPrStats>): String = "" // Keep as is for now

    override actual fun formatReviewerStats(stats: ReviewerReviewStats): String {
        if (stats.reviewedPrStats.isEmpty()) {
            return "⚠ ERROR: No stats to format. No ◫ fancy tables for you! ${Art.SHRUG}"
        }
        return table {
            cellStyle {
                border = true
                alignment = TextAlignment.TopLeft
                paddingLeft = 1
                paddingRight = 1
            }
            header {
                cellStyle {
                    border = false
                    alignment = TextAlignment.TopCenter
                    paddingBottom = 1
                    paddingTop = 2
                }
                row {
                    val headingText =
                        "Stats for all PR reviews given by '${stats.reviewerId}' on '${stats.repoId}' repository " +
                            "between ${appConfig.get().dateLimitAfter} and ${appConfig.get().dateLimitBefore}."
                    val headingSeparator = "-".repeat(headingText.length)
                    cell("$headingSeparator\n$headingText\n$headingSeparator") {
                        columnSpan = 2
                    }
                }
            }

            row("Total Reviews", "${stats.totalReviews}")
            row("Average Review Time", "${stats.average}")

            if (stats.reviewedForPrStats.isNotEmpty()) {
                var itemCount = 1
                stats.reviewedForPrStats.entries
                    .sortedBy { it.value.size }
                    .associate { it.toPair() }
                    .forEach { (userId, prStats) ->
                        val count = prStats.size
                        val statMessage = "✔ $count ${count.prs()} reviewed for '$userId'"
                        if (itemCount == 1) {
                            row {
                                cell("PR Authors Reviewed For") {
                                    rowSpan = stats.reviewedForPrStats.size
                                }
                                cell(statMessage)
                            }
                        } else {
                            row(statMessage)
                        }
                        itemCount++
                    }
            }
        }.toString()
    }

    private fun Int.prs(): String = if (this <= 1) "PR" else "PRs"
    private fun Int.comments(): String = if (this <= 1) "comment" else "comments"
    private fun Int.times(): String = if (this <= 1) "time" else "times"
}
