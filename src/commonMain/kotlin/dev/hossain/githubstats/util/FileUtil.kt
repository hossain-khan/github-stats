package dev.hossain.githubstats.util

import dev.hossain.githubstats.AuthorReviewStats
import dev.hossain.githubstats.PrStats
import dev.hossain.githubstats.UserId
import dev.hossain.githubstats.logging.Log
import dev.hossain.platform.ExpectedPropertiesReader
import dev.hossain.platform.PlatformFile
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

object FileUtil : KoinComponent {
    private const val REPORTS_DIR_PREFIX = "REPORTS"
    private const val REPORT_FILE_PREFIX = "REPORT"
    internal const val REPORT_DIR_AGGREGATE_SUFFIX = "AGGREGATED"
    private const val PATH_SEPARATOR = "/" // Using common path separator

    /**
     * Creates reporting directory path with **repository id** prefixed with it.
     */
    private fun createReportDir(directoryName: String): PlatformFile {
        val repoId = get<ExpectedPropertiesReader>().getRepoId()
        val dirPath = "$REPORTS_DIR_PREFIX-$repoId-$directoryName"
        val directory = PlatformFile(dirPath)
        if (!directory.exists()) {
            directory.mkdirs() // Creates parent directories if they don't exist
            Log.v("The reporting directory ${directory.getAbsolutePath()} created successfully.")
        }
        return directory
    }

    /**
     * Provides HTTP cache directory path that is used for caching responses.
     */
    internal fun httpCacheDir(): PlatformFile {
        val directory = PlatformFile("http-cache")
        if (!directory.exists()) {
            directory.mkdirs()
            Log.v("The HTTP cache directory ${directory.getAbsolutePath()} created successfully.")
        }
        return directory
    }

    private fun PlatformFile.pathWithSeparator(): String = this.getAbsolutePath() + PATH_SEPARATOR

    internal fun authorReportAsciiFile(prAuthorId: UserId): String {
        val dir: PlatformFile = createReportDir(prAuthorId)
        return dir.pathWithSeparator() + "${REPORT_FILE_PREFIX}_-_pr-author-$prAuthorId-ascii.txt"
    }

    internal fun authorPieChartHtmlFile(prAuthorId: UserId): String {
        val dir: PlatformFile = createReportDir(prAuthorId)
        return dir.pathWithSeparator() + "${REPORT_FILE_PREFIX}_-_pr-author-$prAuthorId-pie-chart.html"
    }

    internal fun authorBarChartAggregateHtmlFile(prAuthorId: UserId): String {
        val dir: PlatformFile = createReportDir(prAuthorId)
        return dir.pathWithSeparator() + "${REPORT_FILE_PREFIX}_-_pr-author-$prAuthorId-pr-stats-aggregate.html"
    }

    internal fun allAuthorBarChartAggregateHtmlFile(): String {
        val dir: PlatformFile = createReportDir(REPORT_DIR_AGGREGATE_SUFFIX)
        return dir.pathWithSeparator() + "${REPORT_FILE_PREFIX}_-_aggregated-pr-stats-for-all-authors.html"
    }
    // For FileWriterFormatter - this was added to return a filename for all aggregated authors
    internal fun allAuthorAggregatedReportAsciiFile(): String {
        val dir: PlatformFile = createReportDir(REPORT_DIR_AGGREGATE_SUFFIX)
        return dir.pathWithSeparator() + "${REPORT_FILE_PREFIX}_-_all-authors-aggregated-ascii.txt"
    }


    internal fun authorBarChartHtmlFile(prAuthorId: UserId): String {
        val dir: PlatformFile = createReportDir(prAuthorId)
        return dir.pathWithSeparator() + "${REPORT_FILE_PREFIX}_-_pr-author-$prAuthorId-bar-chart.html"
    }

    internal fun reviewerReportAsciiFile(prReviewerId: UserId): String {
        val dir: PlatformFile = createReportDir(prReviewerId)
        return dir.pathWithSeparator() + "${REPORT_FILE_PREFIX}_-_pr-reviewer-$prReviewerId-ascii.txt"
    }

    internal fun individualPrReportAsciiFile(prStats: PrStats): String {
        val dir: PlatformFile = createReportDir("${prStats.pullRequest.user.login}-PRs")
        return dir.pathWithSeparator() + "$REPORT_FILE_PREFIX-PR-${prStats.pullRequest.number}.txt"
    }

    internal fun individualPrReportHtmlChart(prStats: PrStats): String {
        val dir: PlatformFile = createReportDir("${prStats.pullRequest.user.login}-PRs")
        return dir.pathWithSeparator() + "$REPORT_FILE_PREFIX-PR-${prStats.pullRequest.number}.html"
    }

    internal fun prReviewedForCombinedBarChartFilename(reviewerUserId: UserId): String {
        val dir: PlatformFile = createReportDir(reviewerUserId)
        return dir.pathWithSeparator() + "${REPORT_FILE_PREFIX}_-_prs-reviewed-for-authors-by-$reviewerUserId-bar-chart.html"
    }

    internal fun prReviewedForCombinedFilename(reviewerUserId: UserId): String {
        val dir: PlatformFile = createReportDir(reviewerUserId)
        return dir.pathWithSeparator() + "${REPORT_FILE_PREFIX}_-_prs-reviewed-for-authors-by-$reviewerUserId.csv"
    }

    internal fun repositoryAggregatedPrStatsByAuthorFilename(): String {
        val dir: PlatformFile = createReportDir(REPORT_DIR_AGGREGATE_SUFFIX)
        return dir.pathWithSeparator() + "${REPORT_FILE_PREFIX}_-_aggregated-pr-stats-for-all-authors.csv"
    }

    internal fun prReviewerReviewedPrStatsFile(reviewerUserId: UserId): String {
        val dir: PlatformFile = createReportDir(reviewerUserId)
        return dir.pathWithSeparator() + "${REPORT_FILE_PREFIX}_-_all-prs-reviewed-by-$reviewerUserId.csv"
    }

    internal fun prReviewerReviewedPrStatsBarChartFile(reviewerUserId: UserId): String {
        val dir: PlatformFile = createReportDir(reviewerUserId)
        return dir.pathWithSeparator() + "${REPORT_FILE_PREFIX}_-_all-prs-reviewed-by-$reviewerUserId-bar-chart.html"
    }

    internal fun reviewedForAuthorCsvFile(authorStats: AuthorReviewStats): String {
        val dir: PlatformFile = createReportDir(authorStats.prAuthorId)
        return dir.pathWithSeparator() + "REPORT-all-prs-reviewed-by-${authorStats.reviewerId}-for-${authorStats.prAuthorId}.csv"
    }

    internal fun allReviewersForAuthorFile(prAuthorId: UserId): String {
        val dir: PlatformFile = createReportDir(prAuthorId)
        return dir.pathWithSeparator() + "REPORT_-_$prAuthorId-all-reviewers-total-prs-reviewed.csv"
    }
}
