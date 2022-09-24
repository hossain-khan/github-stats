package dev.hossain.githubstats.formatter

import com.jakewharton.picnic.table
import dev.hossain.githubstats.PullStats
import kotlinx.datetime.toJavaInstant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

/**
 * Uses text based table for console output using [Picnic](https://github.com/JakeWharton/picnic)
 */
class PicnicTableFormatter : StatsFormatter {
    private val dateFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
        .withLocale(Locale.US)
        .withZone(ZoneId.systemDefault())

    override fun formatPrStats(prStats: PullStats.StatsResult.Success): String {
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
}
