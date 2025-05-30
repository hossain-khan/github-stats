package dev.hossain.githubstats.formatter

import dev.hossain.ascii.Art
import dev.hossain.githubstats.AuthorPrStats
import dev.hossain.githubstats.AuthorStats
import dev.hossain.githubstats.PrStats
import dev.hossain.githubstats.ReviewerReviewStats
import dev.hossain.githubstats.util.FileUtil // FileUtil will need refactoring
import dev.hossain.platform.PlatformFile

/**
 * Writes report from given [formatter] into file using [PlatformFile].
 * The main purpose is to use a common [StatsFormatter] (like an expected PicnicTableFormatter)
 * and write the output to file for later reference.
 */
class FileWriterFormatter constructor(
    private val formatter: StatsFormatter,
) : StatsFormatter {
    override fun formatSinglePrStats(prStats: PrStats): String {
        val formattedPrStats = formatter.formatSinglePrStats(prStats)

        // Assuming FileUtil will be refactored to return paths compatible with PlatformFile
        val prStatsFileName = FileUtil.individualPrReportAsciiFile(prStats)
        PlatformFile(prStatsFileName).writeText(formattedPrStats)

        return "Written PR stats to $prStatsFileName" // Provide some feedback
    }

    override fun formatAuthorStats(stats: AuthorStats): String {
        if (stats.reviewStats.isEmpty()) {
            return "⚠ ERROR: No author stats to format. No files to write! ${Art.SHRUG}"
        }

        val prAuthorId = stats.reviewStats.first().prAuthorId
        val formattedStats = formatter.formatAuthorStats(stats)

        val combinedReportFileName = FileUtil.authorReportAsciiFile(prAuthorId)
        PlatformFile(combinedReportFileName).writeText(formattedStats)

        return "Written author stats for $prAuthorId to $combinedReportFileName"
    }

    override fun formatAllAuthorStats(aggregatedPrStats: List<AuthorPrStats>): String {
        // This method was empty. If it needs to write a file, implement similarly.
        // For now, returning a simple message.
        if (aggregatedPrStats.isEmpty()) {
            return "No aggregated stats to format or write."
        }
        val formattedContent = formatter.formatAllAuthorStats(aggregatedPrStats)
        if (formattedContent.isBlank() || formattedContent.startsWith("⚠ ERROR:")) {
            return "Aggregated stats formatting produced no output or an error: $formattedContent"
        }

        // Assuming FileUtil has a method for this report name
        val reportFileName = FileUtil.allAuthorAggregatedReportAsciiFile()
        PlatformFile(reportFileName).writeText(formattedContent)
        return "Written all author aggregated stats to $reportFileName"
    }

    override fun formatReviewerStats(stats: ReviewerReviewStats): String {
        if (stats.reviewedPrStats.isEmpty() || stats.reviewedForPrStats.isEmpty()) {
            return "⚠ ERROR: No reviewer stats to format. No files to write! ${Art.SHRUG}"
        }
        val formattedStats = formatter.formatReviewerStats(stats)

        val combinedReportFileName = FileUtil.reviewerReportAsciiFile(stats.reviewerId)
        PlatformFile(combinedReportFileName).writeText(formattedStats)

        return "Written reviewer stats for ${stats.reviewerId} to $combinedReportFileName"
    }
}
