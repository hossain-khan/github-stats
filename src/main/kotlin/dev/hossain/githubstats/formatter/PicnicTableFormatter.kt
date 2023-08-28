package dev.hossain.githubstats.formatter

import com.jakewharton.picnic.TextAlignment.TopCenter
import com.jakewharton.picnic.TextAlignment.TopLeft
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
import dev.hossain.time.toWorkingHour
import kotlinx.datetime.toJavaInstant
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import kotlin.time.Duration

/**
 * Uses text based table for console output using [Picnic](https://github.com/JakeWharton/picnic)
 */
class PicnicTableFormatter : StatsFormatter, KoinComponent {
    private val dateFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
        .withLocale(Locale.US)
        .withZone(ZoneId.systemDefault())

    private val appConfig: AppConfig by inject()

    /**
     * Formats PR review stats for a specific single PR.
     *
     * Example output:
     * ```
     * ┌──────────────┬──────────────────────────────────────────────────────┐
     * │ Title        │ gh-1019: Implemented prior knowledge approach to h2c │
     * ├──────────────┼──────────────────────────────────────────────────────┤
     * │ PR Author    │ mjpitz                                               │
     * ├──────────────┼──────────────────────────────────────────────────────┤
     * │ URL          │ https://github.com/square/okhttp/pull/3873           │
     * ├──────────────┼──────────────────────────────────────────────────────┤
     * │ Ready On     │ Feb 20, 2018, 5:55:35 PM                             │
     * ├──────────────┼──────────────────────────────────────────────────────┤
     * │ Review Time  │ yschimke=1d 2h 45m                                   │
     * │              ├──────────────────────────────────────────────────────┤
     * │              │ swankjesse=1d 2h 45m                                 │
     * ├──────────────┼──────────────────────────────────────────────────────┤
     * │ PR Comments  │ swankjesse made total 22 comments.                   │
     * │              │ Code Review Comments = 16, Issue Comments = 3        │
     * │              │ Has reviewed PR 3 times.                             │
     * │              ├──────────────────────────────────────────────────────┤
     * │              │ yschimke made total 30 comments.                     │
     * │              │ Code Review Comments = 21, Issue Comments = 9        │
     * ├──────────────┼──────────────────────────────────────────────────────┤
     * │ Merged On    │ Feb 26, 2018, 6:55:11 PM                             │
     * ├──────────────┼──────────────────────────────────────────────────────┤
     * │ Open → Merge │ 6d 0h 59m 36s                                        │
     * └──────────────┴──────────────────────────────────────────────────────┘
     * ```
     */
    override fun formatSinglePrStats(prStats: PrStats): String {
        fun formatUserPrComments(userPrComment: UserPrComment) =
            "${userPrComment.user} made total ${userPrComment.allComments} ${userPrComment.allComments.comments()}.\n" +
                "Code Review Comments = ${userPrComment.codeReviewComment}, " +
                "Issue Comments = ${userPrComment.issueComment}" +
                if (userPrComment.prReviewSubmissionComment > 0) "\nHas reviewed PR ${userPrComment.prReviewSubmissionComment} ${userPrComment.prReviewSubmissionComment.times()}." else ""

        fun formatUserDuration(userDuration: Map.Entry<UserId, Duration>): String {
            return "$userDuration | ${userDuration.value.toWorkingHour()}"
        }

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
                // This row has only one cell because earlier data will carry over and push it to the right.
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
                // This row has only one cell because earlier data will carry over and push it to the right.
                prStats.prApprovalTime.entries.drop(1).forEach {
                    row(formatUserDuration(it))
                }
            }
            if (prStats.comments.isNotEmpty()) {
                row {
                    cell("PR Comments") {
                        rowSpan = prStats.comments.size
                    }
                    cell(formatUserPrComments(prStats.comments.entries.first().value))
                }
                // This row has only one cell because earlier data will carry over and push it to the right.
                prStats.comments.entries.drop(1).forEach {
                    row(formatUserPrComments(it.value))
                }
            }
            row("Merged On", dateFormatter.format(prStats.prMergedOn.toJavaInstant()))
            row("Open → Merge", "${prStats.prMergedOn - prStats.prReadyOn}")
        }.toString()
    }

    /**
     * Formats PR review stats for list of users that reviewed specific user's PRs.
     *
     * Sample output:
     * ```
     *   -------------------------------------------------------------------------------------------------
     *   PR reviewer's stats for PR created by 'naomi-lgbt' on 'freeCodeCamp' repository since 2022-10-01.
     *   -------------------------------------------------------------------------------------------------
     *
     * ┌────────────────────────────────────────────────────────────────────────────────────────────────────────────┐
     * │                                                                                                            │
     * │ ● PR reviewer stats for "raisedadead"                                                                      │
     * ├───────────────────────────────────────────────┬────────────────────────────────────────────────────────────┤
     * │ Total Reviews                                 │ 6                                                          │
     * ├───────────────────────────────────────────────┼────────────────────────────────────────────────────────────┤
     * │ Review Durations                              │ 1h for PR#48046                                            │
     * │                                               ├────────────────────────────────────────────────────────────┤
     * │                                               │ 25m 52s for PR#48045                                       │
     * │                                               │ made 18 code review comments and 2 issue comments.         │
     * │                                               ├────────────────────────────────────────────────────────────┤
     * │                                               │ 7h 3m for PR#47685                                         │
     * │                                               │ made 11 code review comments and 2 issue comments.         │
     * │                                               ├────────────────────────────────────────────────────────────┤
     * │                                               │ 0s for PR#47885                                            │
     * │                                               ├────────────────────────────────────────────────────────────┤
     * │                                               │ 3h for PR#47807                                            │
     * ├───────────────────────────────────────────────┼────────────────────────────────────────────────────────────┤
     * │ Average Time                                  │ 44m 18.66s                                                 │
     * ├───────────────────────────────────────────────┴────────────────────────────────────────────────────────────┤
     * │                                                                                                            │
     * │ ● PR reviewer stats for "Sboonny"                                                                          │
     * ├───────────────────────────────────────────────┬────────────────────────────────────────────────────────────┤
     * │ Total Reviews                                 │ 3                                                          │
     * ├───────────────────────────────────────────────┼────────────────────────────────────────────────────────────┤
     * │ Review Durations                              │ 3h for PR#48297                                            │
     * │                                               │ made 6 code review comments and 2 issue comments.          │
     * │                                               ├────────────────────────────────────────────────────────────┤
     * │                                               │ 0s for PR#48271                                            │
     * │                                               ├────────────────────────────────────────────────────────────┤
     * │                                               │ 0s for PR#47940                                            │
     * ├───────────────────────────────────────────────┼────────────────────────────────────────────────────────────┤
     * │ Average Time                                  │ 1h                                                         │
     * └───────────────────────────────────────────────┴────────────────────────────────────────────────────────────┘
     * ```
     */
    override fun formatAuthorStats(stats: AuthorStats): String {
        if (stats.reviewStats.isEmpty()) {
            return "⚠ ERROR: No stats to format. No ◫ fancy tables for you! ${Art.shrug}"
        }

        /**
         * Internal function to format PR review time and review comments count.
         */
        fun formatPrReviewTimeAndComments(reviewStats: ReviewStats): String {
            return "${reviewStats.reviewCompletion} for PR#${reviewStats.pullRequest.number}" +
                if (reviewStats.prComments.isEmpty().not()) {
                    "\nmade ${reviewStats.prComments.codeReviewComment} code review ${reviewStats.prComments.codeReviewComment.comments()} " +
                        "and ${reviewStats.prComments.issueComment} issue ${reviewStats.prComments.issueComment.comments()}."
                } else {
                    "" // When no comment metrics is available, don't show it.
                } + if (reviewStats.prComments.prReviewSubmissionComment > 0) "\nalso has reviewed PR ${reviewStats.prComments.prReviewSubmissionComment} ${reviewStats.prComments.prReviewSubmissionComment.times()}." else ""
        }

        val repoId = stats.reviewStats.first().repoId
        val prAuthorId = stats.reviewStats.first().prAuthorId

        return table {
            cellStyle {
                border = true
                alignment = TopLeft
                paddingLeft = 1
                paddingRight = 1
            }

            // Provide global info about this stats
            header {
                cellStyle {
                    border = false
                    alignment = TopCenter
                    paddingBottom = 1
                    paddingTop = 2
                }
                row {
                    // Export global info for the stats
                    val headingText = "PR reviewer's stats for PRs created by '$prAuthorId' on '$repoId' repository " +
                        "between ${appConfig.get().dateLimitAfter} and ${appConfig.get().dateLimitBefore}."
                    val headingSeparator = "-".repeat(headingText.length)
                    cell("$headingSeparator\n$headingText\n$headingSeparator") {
                        columnSpan = 2
                    }
                }
            }

            stats.reviewStats.forEach { stat ->
                // Author header item for all the author specific stats
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
                // This row has only one cell because earlier data will carry over and push it to the right.
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
                cell("${stats.reviewStats.avgMergeTime()}")
            }
        }.toString()
    }

    override fun formatAllAuthorStats(aggregatedPrStats: List<AuthorPrStats>): String {
        return ""
    }

    /**
     * Formats [ReviewerReviewStats] that contains all review stats given by the reviewer.
     *
     * Sample output:
     * ```
     *   ---------------------------------------------------------------------------------------------
     *   Stats for all PR reviews given by 'naomi-lgbt' on 'freeCodeCamp' repository since 2022-10-01.
     *   ---------------------------------------------------------------------------------------------
     *
     * ┌──────────────────────────────┬─────────────────────────────────────────────┐
     * │ Total Reviews                │ 33                                          │
     * ├──────────────────────────────┼─────────────────────────────────────────────┤
     * │ Average Review Time          │ 7h 20m 58.68s                               │
     * ├──────────────────────────────┼─────────────────────────────────────────────┤
     * │ PR Authors Reviewed For      │ ✔ 1 PR reviewed for 'miyaliu666'            │
     * │                              ├─────────────────────────────────────────────┤
     * │                              │ ✔ 2 PRs reviewed for 'DerrykBoyd'           │
     * │                              ├─────────────────────────────────────────────┤
     * │                              │ ✔ 3 PRs reviewed for 'nayabatir1'           │
     * │                              ├─────────────────────────────────────────────┤
     * │                              │ ✔ 3 PRs reviewed for 'ojeytonwilliams'      │
     * │                              ├─────────────────────────────────────────────┤
     * │                              │ ✔ 4 PRs reviewed for 'raisedadead'          │
     * │                              ├─────────────────────────────────────────────┤
     * │                              │ ✔ 5 PRs reviewed for 'Sboonny'              │
     * │                              ├─────────────────────────────────────────────┤
     * │                              │ ✔ 15 PRs reviewed for 'camperbot'           │
     * └──────────────────────────────┴─────────────────────────────────────────────┘
     * ```
     */
    override fun formatReviewerStats(stats: ReviewerReviewStats): String {
        if (stats.reviewedPrStats.isEmpty()) {
            return "⚠ ERROR: No stats to format. No ◫ fancy tables for you! ${Art.shrug}"
        }
        return table {
            cellStyle {
                border = true
                alignment = TopLeft
                paddingLeft = 1
                paddingRight = 1
            }

            // Provide global info about this stats
            header {
                cellStyle {
                    border = false
                    alignment = TopCenter
                    paddingBottom = 1
                    paddingTop = 2
                }
                row {
                    val headingText = "Stats for all PR reviews given by '${stats.reviewerId}' on '${stats.repoId}' repository " +
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
                    .sortedBy { it.value.size }.associate { it.toPair() }
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

    /** Internal function to use plurals for PR. */
    private fun Int.prs(): String = if (this <= 1) "PR" else "PRs"

    /** Internal function to use plurals for comments. */
    private fun Int.comments(): String = if (this <= 1) "comment" else "comments"

    /** Internal function to use plurals for `times`. */
    private fun Int.times(): String = if (this <= 1) "time" else "times"
}
