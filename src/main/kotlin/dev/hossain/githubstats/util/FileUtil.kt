package dev.hossain.githubstats.util

import dev.hossain.githubstats.AuthorReviewStats
import dev.hossain.githubstats.PrStats
import dev.hossain.githubstats.UserId
import dev.hossain.githubstats.logging.Log
import org.koin.core.component.KoinComponent
import java.io.File

object FileUtil : KoinComponent {
    private const val REPORTS_DIR_PREFIX = "REPORTS"
    private const val REPORT_FILE_PREFIX = "REPORT"
    internal const val REPORT_DIR_AGGREGATE_SUFFIX = "AGGREGATED"

    /**
     * Creates reporting directory path with **repository id** prefixed with it.
     *
     * Example dirs for `freeCodeCamp` repository:
     * - `REPORTS-freeCodeCamp-DanielRosa74`
     * - `REPORTS-freeCodeCamp-ojeytonwilliams`
     * - `REPORTS-freeCodeCamp-AGGREGATED`
     */
    private fun createReportDir(directoryName: String): File {
        val repoId = getKoin().get<LocalProperties>().getRepoId()
        val directory = File("$REPORTS_DIR_PREFIX-$repoId-$directoryName")
        if (directory.exists().not() && directory.mkdir()) {
            Log.v("The reporting directory ${directory.path} created successfully.")
        }
        return directory
    }

    /**
     * Provides HTTP cache directory path that is used for caching responses.
     */
    internal fun httpCacheDir(): File {
        val directory = File("http-cache")
        if (directory.exists().not() && directory.mkdir()) {
            Log.v("The HTTP cache directory ${directory.path} created successfully.")
        }
        return directory
    }

    /**
     * Contains PR reviewer's stats for PRs created by PR author on repository between configured dates.
     *
     * Example:
     * - `REPORT_-_pr-author-naomi-lgbt-ascii.txt`
     *
     * @see authorPieChartHtmlFile
     */
    internal fun authorReportAsciiFile(prAuthorId: UserId): String {
        val dir: File = createReportDir(prAuthorId)
        return dir.path() + "${REPORT_FILE_PREFIX}_-_pr-author-$prAuthorId-ascii.txt"
    }

    /**
     * HTML Chart file for author stats.
     *
     * Example:
     * - `REPORT_-_pr-author-naomi-lgbt-pie-chart.html`
     *
     * @see authorReportAsciiFile
     */
    internal fun authorPieChartHtmlFile(prAuthorId: UserId): String {
        val dir: File = createReportDir(prAuthorId)
        return dir.path() + "${REPORT_FILE_PREFIX}_-_pr-author-$prAuthorId-pie-chart.html"
    }

    /**
     * HTML Chart file for author PR stats that is aggregated.
     *
     * Example:
     * - `REPORT_-_pr-author-naomi-lgbt-pr-stats-aggregate.html`
     *
     * @see authorReportAsciiFile
     */
    internal fun authorBarChartAggregateHtmlFile(prAuthorId: UserId): String {
        val dir: File = createReportDir(prAuthorId)
        return dir.path() + "${REPORT_FILE_PREFIX}_-_pr-author-$prAuthorId-pr-stats-aggregate.html"
    }

    /**
     * HTML Chart file for all author aggregated stats.
     *
     * Example:
     * - `REPORT_-_aggregated-pr-stats-for-all-authors.html`
     *
     * @see authorReportAsciiFile
     * @see repositoryAggregatedPrStatsByAuthorFilename
     */
    internal fun allAuthorBarChartAggregateHtmlFile(): String {
        val dir: File = createReportDir(REPORT_DIR_AGGREGATE_SUFFIX)
        return dir.path() + "${REPORT_FILE_PREFIX}_-_aggregated-pr-stats-for-all-authors.html"
    }

    /**
     * HTML Chart file for author stats.
     * Contains PR reviewer`s stats for PRs created by author on repository between configured dates.
     *
     * Example:
     * - `REPORT_-_pr-author-naomi-lgbt-bar-chart.html`
     *
     * @see authorReportAsciiFile
     */
    internal fun authorBarChartHtmlFile(prAuthorId: UserId): String {
        val dir: File = createReportDir(prAuthorId)
        return dir.path() + "${REPORT_FILE_PREFIX}_-_pr-author-$prAuthorId-bar-chart.html"
    }

    /**
     * Stats for all PR reviews given by user on repository between configured dates.
     *
     * Example:
     * - `REPORT_-_pr-reviewer-naomi-lgbt-ascii.txt`
     */
    internal fun reviewerReportAsciiFile(prReviewerId: UserId): String {
        val dir: File = createReportDir(prReviewerId)
        return dir.path() + "${REPORT_FILE_PREFIX}_-_pr-reviewer-$prReviewerId-ascii.txt"
    }

    /**
     * Generates individual PR stats ascii file.
     *
     * Example:
     * - `REPORTS-freeCodeCamp-naomi-lgbt-PRs/REPORT-PR-51018.txt`
     *
     * @see individualPrReportHtmlChart
     */
    internal fun individualPrReportAsciiFile(prStats: PrStats): String {
        val dir: File = createReportDir("${prStats.pullRequest.user.login}-PRs")

        return dir.path() + "$REPORT_FILE_PREFIX-PR-${prStats.pullRequest.number}.txt"
    }

    /**
     * Generates individual PR stats html chart file.
     *
     * Example:
     * - `REPORTS-freeCodeCamp-naomi-lgbt-PRs/REPORT-PR-51018.html`
     *
     * @see individualPrReportAsciiFile
     */
    internal fun individualPrReportHtmlChart(prStats: PrStats): String {
        val dir: File = createReportDir("${prStats.pullRequest.user.login}-PRs")

        return dir.path() + "$REPORT_FILE_PREFIX-PR-${prStats.pullRequest.number}.html"
    }

    /**
     * Bar chart file for PRs reviewed by user for other users.
     *
     * Example
     * - `REPORT_-_prs-reviewed-for-authors-by-Sboonny-bar-chart.html`
     *
     * @see prReviewedForCombinedFilename
     */
    internal fun prReviewedForCombinedBarChartFilename(reviewerUserId: UserId): String {
        val dir: File = createReportDir(reviewerUserId)
        return dir.path() + "${REPORT_FILE_PREFIX}_-_prs-reviewed-for-authors-by-$reviewerUserId-bar-chart.html"
    }

    /**
     * Ascii report file for PRs reviewed by user for other users.
     *
     * Example:
     * - `REPORT_-_prs-reviewed-for-authors-by-Sboonny.csv`
     *
     * @see prReviewedForCombinedBarChartFilename
     */
    internal fun prReviewedForCombinedFilename(reviewerUserId: UserId): String {
        val dir: File = createReportDir(reviewerUserId)
        return dir.path() + "${REPORT_FILE_PREFIX}_-_prs-reviewed-for-authors-by-$reviewerUserId.csv"
    }

    /**
     * This is the file name for the repository's aggregated stats of all PRs created by user.
     *
     * Sample file names:
     * - `REPORT_-_aggregated-pr-stats-for-all-authors.csv`
     *
     * @see allAuthorBarChartAggregateHtmlFile
     */
    internal fun repositoryAggregatedPrStatsByAuthorFilename(): String {
        val dir: File = createReportDir(REPORT_DIR_AGGREGATE_SUFFIX)
        return dir.path() + "${REPORT_FILE_PREFIX}_-_aggregated-pr-stats-for-all-authors.csv"
    }

    /**
     * Ascii report file for all PRs reviewed by user.
     *
     * Example:
     * - `REPORT_-_all-prs-reviewed-by-Sboonny.csv`
     *
     * @see prReviewerReviewedPrStatsBarChartFile
     */
    internal fun prReviewerReviewedPrStatsFile(reviewerUserId: UserId): String {
        val dir: File = createReportDir(reviewerUserId)
        return dir.path() + "${REPORT_FILE_PREFIX}_-_all-prs-reviewed-by-$reviewerUserId.csv"
    }

    /**
     * Bar chart html file for all PRs reviewed by user.
     *
     * Example:
     * - `REPORT_-_all-prs-reviewed-by-Sboonny-bar-chart.html`
     *
     * @see prReviewerReviewedPrStatsFile
     */
    internal fun prReviewerReviewedPrStatsBarChartFile(reviewerUserId: UserId): String {
        val dir: File = createReportDir(reviewerUserId)
        return dir.path() + "${REPORT_FILE_PREFIX}_-_all-prs-reviewed-by-$reviewerUserId-bar-chart.html"
    }

    /**
     * CSV data for all reviews given to specific author.
     *
     * Example
     * - `REPORT-all-prs-reviewed-by-ojeytonwilliams-for-Sboonny.csv`
     * - `REPORT-all-prs-reviewed-by-huyenltnguyen-for-Sboonny.csv`
     * - `REPORT-all-prs-reviewed-by-raisedadead-for-Sboonny.csv`
     */
    internal fun reviewedForAuthorCsvFile(authorStats: AuthorReviewStats): String {
        val dir: File = createReportDir(authorStats.prAuthorId)
        return dir.path() + "REPORT-all-prs-reviewed-by-${authorStats.reviewerId}-for-${authorStats.prAuthorId}.csv"
    }

    /**
     * Total PR Reviewed for user by different reviewers between configured dates.
     *
     * Example:
     * - `REPORT_-_Sboonny-all-reviewers-total-prs-reviewed.csv`
     */
    internal fun allReviewersForAuthorFile(prAuthorId: UserId): String {
        val dir: File = createReportDir(prAuthorId)
        return dir.path() + "REPORT_-_$prAuthorId-all-reviewers-total-prs-reviewed.csv"
    }

    /**
     * Internal extension function to provide file path followed by the path separator
     */
    private fun File.path(): String = this.path + File.separator
}
