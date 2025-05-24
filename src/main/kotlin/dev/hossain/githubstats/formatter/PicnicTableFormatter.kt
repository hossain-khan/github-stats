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
import dev.hossain.i18n.Resources
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
class PicnicTableFormatter(
    private val resources: Resources,
) : StatsFormatter, KoinComponent {
    private val dateFormatter =
        DateTimeFormatter
            .ofLocalizedDateTime(FormatStyle.MEDIUM)
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
                if (userPrComment.prReviewSubmissionComment > 0) {
                    "\nHas reviewed PR ${userPrComment.prReviewSubmissionComment} ${userPrComment.prReviewSubmissionComment.times()}."
                } else {
                    ""
                }

        fun formatUserDuration(userDuration: Map.Entry<UserId, Duration>): String = "$userDuration | ${userDuration.value.toWorkingHour()}" // TODO: Localize pipe

        return table {
            cellStyle {
                border = true
                paddingLeft = 1
                paddingRight = 1
            }
            row(resources.string("picnic_tbl_header_title"), prStats.pullRequest.title)
            row(resources.string("picnic_tbl_header_pr_author"), prStats.pullRequest.user.login)
            row(resources.string("picnic_tbl_header_url"), prStats.pullRequest.html_url)
            row(resources.string("picnic_tbl_header_ready_on"), dateFormatter.format(prStats.prReadyOn.toJavaInstant()))
            if (prStats.initialResponseTime.isNotEmpty()) {
                row {
                    cell(resources.string("picnic_tbl_header_initial_response_time")) {
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
                    cell(resources.string("picnic_tbl_header_approval_time")) {
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
                    cell(resources.string("picnic_tbl_header_pr_comments")) {
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
                // This row has only one cell because earlier data will carry over and push it to the right.
                prStats.comments.entries.drop(1).forEach {
                    row(formatUserPrComments(it.value))
                }
            }
            row(resources.string("picnic_tbl_header_merged_on"), dateFormatter.format(prStats.prMergedOn.toJavaInstant()))
            row(resources.string("picnic_tbl_header_open_to_merge"), "${prStats.prMergedOn - prStats.prReadyOn}")
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
            return resources.string("picnic_error_no_stats_to_format", resources.string("art_shrug"))
        }

        /**
         * Internal function to format PR review time and review comments count.
         */
        @Suppress("ktlint:standard:max-line-length")
        fun formatPrReviewTimeAndComments(reviewStats: ReviewStats): String {
            val codeComments = reviewStats.prComments.codeReviewComment
            val issueComments = reviewStats.prComments.issueComment
            val submissionComments = reviewStats.prComments.prReviewSubmissionComment

            val commentsMadeStr = if (reviewStats.prComments.isEmpty().not()) {
                resources.string(
                    "picnic_X_code_comments_and_Y_issue_comments",
                    codeComments,
                    codeComments.comments(),
                    issueComments,
                    issueComments.comments(),
                )
            } else {
                "" // When no comment metrics is available, don't show it.
            }
            val reviewedPrStr = if (submissionComments > 0) {
                resources.string("picnic_also_has_reviewed_pr_X_times", submissionComments, submissionComments.times())
            } else {
                ""
            }

            return resources.string(
                "picnic_review_time_X_for_pr_Y",
                reviewStats.reviewCompletion,
                reviewStats.pullRequest.number,
            ) + commentsMadeStr + reviewedPrStr
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
                    val headingText = resources.string(
                        "picnic_header_author_stats",
                        prAuthorId,
                        repoId,
                        appConfig.get().dateLimitAfter,
                        appConfig.get().dateLimitBefore,
                    )
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
                    cell(resources.string("picnic_reviewer_stats_for_X", stat.reviewerId)) {
                        columnSpan = 2
                    }
                }

                row(resources.string("picnic_tbl_header_total_reviews"), "${stat.totalReviews}")
                row {
                    cell(resources.string("picnic_tbl_header_review_durations")) {
                        rowSpan = stat.stats.size
                    }
                    cell(formatPrReviewTimeAndComments(stat.stats.first()))
                }
                // This row has only one cell because earlier data will carry over and push it to the right.
                stat.stats.drop(1).forEach {
                    row(formatPrReviewTimeAndComments(it))
                }
                row(resources.string("picnic_tbl_header_average_time"), "${stat.average}")
            }

            row {
                cellStyle {
                    paddingTop = 2
                }
                cell(resources.string("picnic_avg_pr_merge_time_for_X", prAuthorId))
                cell("${stats.reviewStats.avgMergeTime()}")
            }
        }.toString()
    }

    override fun formatAllAuthorStats(aggregatedPrStats: List<AuthorPrStats>): String = ""

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
            return resources.string("picnic_error_no_stats_to_format", resources.string("art_shrug"))
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
                    val headingText = resources.string(
                        "picnic_header_reviewer_stats",
                        stats.reviewerId,
                        stats.repoId,
                        appConfig.get().dateLimitAfter,
                        appConfig.get().dateLimitBefore,
                    )
                    val headingSeparator = "-".repeat(headingText.length)
                    cell("$headingSeparator\n$headingText\n$headingSeparator") {
                        columnSpan = 2
                    }
                }
            }

            row(resources.string("picnic_tbl_header_total_reviews"), "${stats.totalReviews}")
            row(resources.string("picnic_tbl_header_average_review_time"), "${stats.average}")

            if (stats.reviewedForPrStats.isNotEmpty()) {
                var itemCount = 1
                stats.reviewedForPrStats.entries
                    .sortedBy { it.value.size }
                    .associate { it.toPair() }
                    .forEach { (userId, prStats) ->
                        val count = prStats.size
                        val statMessage = resources.string("picnic_X_prs_reviewed_for_Y", count, count.prs(), userId)
                        if (itemCount == 1) {
                            row {
                                cell(resources.string("picnic_tbl_header_pr_authors_reviewed_for")) {
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
    private fun Int.prs(): String = if (this <= 1) resources.string("picnic_pr") else resources.string("picnic_prs")

    /** Internal function to use plurals for comments. */
    private fun Int.comments(): String = if (this <= 1) resources.string("picnic_comment") else resources.string("picnic_comments")

    /** Internal function to use plurals for `times`. */
    private fun Int.times(): String = if (this <= 1) resources.string("picnic_time") else resources.string("picnic_times")
}
