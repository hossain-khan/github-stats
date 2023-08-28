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
     * Creates reporting directory path with known prefix.
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
     * Provides HTTP cache directory path.
     */
    internal fun httpCacheDir(): File {
        val directory = File("http-cache")
        if (directory.exists().not() && directory.mkdir()) {
            Log.v("The HTTP cache directory ${directory.path} created successfully.")
        }
        return directory
    }

    /**
     * @see authorPieChartFile
     */
    internal fun authorReportFile(prAuthorId: UserId): String {
        val dir: File = createReportDir(prAuthorId)
        return dir.path() + "${REPORT_FILE_PREFIX}_-_pr-author-$prAuthorId-ascii.txt"
    }

    /**
     * HTML Chart file for author stats.
     * @see authorReportFile
     */
    internal fun authorPieChartFile(prAuthorId: UserId): String {
        val dir: File = createReportDir(prAuthorId)
        return dir.path() + "${REPORT_FILE_PREFIX}_-_pr-author-$prAuthorId-pie-chart.html"
    }

    /**
     * HTML Chart file for author stats.
     * @see authorReportFile
     */
    internal fun authorBarChartAggregateFile(prAuthorId: UserId): String {
        val dir: File = createReportDir(prAuthorId)
        return dir.path() + "${REPORT_FILE_PREFIX}_-_pr-author-$prAuthorId-pr-stats-aggregate.html"
    }

    /**
     * HTML Chart file for author stats.
     * @see authorReportFile
     */
    internal fun authorBarChartFile(prAuthorId: UserId): String {
        val dir: File = createReportDir(prAuthorId)
        return dir.path() + "${REPORT_FILE_PREFIX}_-_pr-author-$prAuthorId-bar-chart.html"
    }

    internal fun reviewerReportFile(prReviewerId: UserId): String {
        val dir: File = createReportDir(prReviewerId)
        return dir.path() + "${REPORT_FILE_PREFIX}_-_pr-reviewer-$prReviewerId-ascii.txt"
    }

    internal fun prReportFile(prStats: PrStats): String {
        val dir: File = createReportDir("${prStats.pullRequest.user.login}-PRs")

        return dir.path() + "$REPORT_FILE_PREFIX-PR-${prStats.pullRequest.number}.txt"
    }

    /**
     * @see prReportFile
     */
    internal fun prReportChart(prStats: PrStats): String {
        val dir: File = createReportDir("${prStats.pullRequest.user.login}-PRs")

        return dir.path() + "$REPORT_FILE_PREFIX-PR-${prStats.pullRequest.number}.html"
    }

    /**
     * @see prReviewedForCombinedFilename
     */
    internal fun prReviewedForCombinedBarChartFilename(reviewerUserId: UserId): String {
        val dir: File = createReportDir(reviewerUserId)
        return dir.path() + "${REPORT_FILE_PREFIX}_-_prs-reviewed-for-authors-by-$reviewerUserId-bar-chart.html"
    }

    internal fun prReviewedForCombinedFilename(reviewerUserId: UserId): String {
        val dir: File = createReportDir(reviewerUserId)
        return dir.path() + "${REPORT_FILE_PREFIX}_-_prs-reviewed-for-authors-by-$reviewerUserId.csv"
    }

    /**
     * This is the file name for the repository's aggregated stats of all PRs created by user.
     *
     * Sample file names:
     * - `REPORT_-_aggregated-pr-stats-for-all-authors-on-XYZ-repo.csv`
     */
    internal fun repositoryAggregatedPrStatsByAuthorFilename(repoId: String): String {
        val dir: File = createReportDir(repoId)
        return dir.path() + "${REPORT_FILE_PREFIX}_-_aggregated-pr-stats-for-all-authors-on-$repoId-repo.csv"
    }

    internal fun prReviewerReviewedPrStatsFile(reviewerUserId: UserId): String {
        val dir: File = createReportDir(reviewerUserId)
        return dir.path() + "${REPORT_FILE_PREFIX}_-_all-prs-reviewed-by-$reviewerUserId.csv"
    }

    /**
     * @see prReviewerReviewedPrStatsFile
     */
    internal fun prReviewerReviewedPrStatsBarChartFile(reviewerUserId: UserId): String {
        val dir: File = createReportDir(reviewerUserId)
        return dir.path() + "${REPORT_FILE_PREFIX}_-_all-prs-reviewed-by-$reviewerUserId-bar-chart.html"
    }

    internal fun reviewedForAuthorFileName(authorStats: AuthorReviewStats): String {
        val dir: File = createReportDir(authorStats.prAuthorId)
        return dir.path() + "REPORT-${authorStats.reviewerId}-for-${authorStats.prAuthorId}.csv"
    }

    internal fun allReviewersForAuthorFile(prAuthorId: UserId): String {
        val dir: File = createReportDir(prAuthorId)
        return dir.path() + "REPORT_-_$prAuthorId-all-reviewers.csv"
    }

    /**
     * Internal extension function to provide file path followed by the path separator
     */
    private fun File.path(): String = this.path + File.separator
}
