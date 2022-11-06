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
     * @see authorChartFile
     */
    internal fun authorReportFile(prAuthorId: UserId): String {
        val directory: File = createReportDir(prAuthorId)
        return directory.path + File.separator + "${REPORT_FILE_PREFIX}_-_pr-author-$prAuthorId-ascii.txt"
    }

    /**
     * HTML Chart file for author stats.
     * @see authorReportFile
     */
    internal fun authorChartFile(prAuthorId: UserId): String {
        val directory: File = createReportDir(prAuthorId)
        return directory.path + File.separator + "${REPORT_FILE_PREFIX}_-_pr-author-$prAuthorId.html"
    }

    internal fun reviewerReportFile(prReviewerId: UserId): String {
        val directory: File = createReportDir(prReviewerId)
        return directory.path + File.separator + "${REPORT_FILE_PREFIX}_-_pr-reviewer-$prReviewerId-ascii.txt"
    }

    internal fun prReportFile(prStats: PrStats): String {
        val directory: File = createReportDir("${prStats.pullRequest.user.login}-PRs")

        return directory.path + File.separator + "$REPORT_FILE_PREFIX-PR-${prStats.pullRequest.number}.txt"
    }

    /**
     * @see prReportFile
     */
    internal fun prReportChart(prStats: PrStats): String {
        val directory: File = createReportDir("${prStats.pullRequest.user.login}-PRs")

        return directory.path + File.separator + "$REPORT_FILE_PREFIX-PR-${prStats.pullRequest.number}.html"
    }

    internal fun prReviewedForCombinedFilename(reviewerUserId: UserId): String {
        val directory: File = createReportDir(reviewerUserId)
        return directory.path + File.separator + "${REPORT_FILE_PREFIX}_-_prs-reviewed-for-authors-by-$reviewerUserId.csv"
    }

    internal fun prReviewerReviewedPrStatsFile(reviewerUserId: UserId): String {
        val directory: File = createReportDir(reviewerUserId)
        return directory.path + File.separator + "${REPORT_FILE_PREFIX}_-_all-prs-reviewed-by-$reviewerUserId.csv"
    }

    internal fun reviewedForAuthorFileName(authorStats: AuthorReviewStats): String {
        val directory: File = createReportDir(authorStats.prAuthorId)
        return directory.path + File.separator + "REPORT-${authorStats.reviewerId}-for-${authorStats.prAuthorId}.csv"
    }

    internal fun allReviewersForAuthorFile(prAuthorId: UserId): String {
        val directory: File = createReportDir(prAuthorId)
        return directory.path + File.separator + "REPORT_-_$prAuthorId-all-reviewers.csv"
    }
}
