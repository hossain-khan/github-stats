package dev.hossain.githubstats.formatter

import com.jakewharton.picnic.TextAlignment.TopCenter
import com.jakewharton.picnic.TextAlignment.TopLeft
import com.jakewharton.picnic.table
import dev.hossain.githubstats.AuthorReviewStats
import dev.hossain.githubstats.PrStats
import dev.hossain.githubstats.ReviewerReviewStats
import kotlinx.datetime.toJavaInstant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

/**
 * Uses text based table for console output using [Picnic](https://github.com/JakeWharton/picnic)
 */
class PicnicTableFormatter constructor(
    private val zoneId: ZoneId,
    private val dateLimit: String
) : StatsFormatter {
    private val dateFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
        .withLocale(Locale.US)
        .withZone(zoneId)

    override fun formatPrStats(prStats: PrStats): String {
        return table {
            cellStyle {
                border = true
                paddingLeft = 1
                paddingRight = 1
            }
            row("Title", prStats.pullRequest.title)
            row("URL", prStats.pullRequest.html_url)
            row("Ready On", dateFormatter.format(prStats.prReadyOn.toJavaInstant()))
            if (prStats.reviewTime.isNotEmpty()) {
                row {
                    cell("Review Time") {
                        rowSpan = prStats.reviewTime.size
                    }
                    cell("${prStats.reviewTime.entries.first()}")
                }
                // This row has only one cell because earlier data will carry over and push it to the right.
                prStats.reviewTime.entries.drop(1).forEach {
                    row("$it")
                }
            }
            row("Merged On", dateFormatter.format(prStats.prMergedOn.toJavaInstant()))
            row("Open → Merge", "${prStats.prMergedOn - prStats.prReadyOn}")
        }.toString()
    }

    override fun formatAuthorStats(stats: List<AuthorReviewStats>): String {
        if (stats.isEmpty()) {
            return "⚠ ERROR: No stats to format. No ◫ fancy tables for you! ¯\\_(ツ)_/¯"
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
                    // Export global info for the stats
                    val prAuthorId = stats.first().prAuthorId
                    val repoId = stats.first().repoId
                    val headingText = "PR reviewer's stats for PR created by '$prAuthorId' on '$repoId' repository since $dateLimit."
                    val headingSeparator = "-".repeat(headingText.length)
                    cell("$headingSeparator\n$headingText\n$headingSeparator") {
                        columnSpan = 2
                    }
                }
            }

            stats.forEach { stat ->
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
                    cell("${stat.stats.first().reviewCompletion} for PR#${stat.stats.first().pullRequest.number}")
                }
                // This row has only one cell because earlier data will carry over and push it to the right.
                stat.stats.drop(1).forEach {
                    row("${it.reviewCompletion} for PR#${it.pullRequest.number}")
                }
                row("Average Time", "${stat.average}")
            }
        }.toString()
    }

    override fun formatReviewerStats(stats: ReviewerReviewStats): String {
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
                    val headingText = "Stats for all PR reviews given by '${stats.reviewerId}' on '${stats.repoId}' repository since $dateLimit."
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
}
