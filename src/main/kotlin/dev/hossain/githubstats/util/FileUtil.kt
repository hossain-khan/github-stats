package dev.hossain.githubstats.util

import dev.hossain.githubstats.BuildConfig
import dev.hossain.githubstats.PrStats
import dev.hossain.githubstats.UserId
import java.io.File

object FileUtil {
    private const val REPORTS_DIR_PREFIX = "REPORTS"
    private const val REPORT_FILE_PREFIX = "REPORT"

    internal fun createReportDir(directoryName: String): File {
        val directory = File("$REPORTS_DIR_PREFIX-$directoryName")
        if (directory.exists().not() && directory.mkdir()) {
            if (BuildConfig.DEBUG) {
                println("The reporting directory ${directory.path} created successfully.")
            }
        }
        return directory
    }

    internal fun authorReportFile(prAuthorId: UserId): String {
        val directory: File = createReportDir(prAuthorId)
        return directory.path + File.separator + "${REPORT_FILE_PREFIX}_-_$prAuthorId.txt"
    }

    internal fun prReportFile(prStats: PrStats): String {
        val directory: File = createReportDir("${prStats.pullRequest.user.login}-PRs")

        return directory.path + File.separator + "$REPORT_FILE_PREFIX-PR-${prStats.pullRequest.number}.txt"
    }

    internal fun prReviewedForCombinedFilename(reviewerUserId: UserId): String {
        val directory: File = createReportDir(reviewerUserId)
        return directory.path + File.separator + "${REPORT_FILE_PREFIX}_-_prs-reviewed-for-authors-by-$reviewerUserId.csv"
    }

    internal fun prReviewerReviewedPrStatsFile(reviewerUserId: UserId): String {
        val directory: File = createReportDir(reviewerUserId)
        return directory.path + File.separator + "${REPORT_FILE_PREFIX}_-_all-prs-reviewed-by-$reviewerUserId.csv"
    }
}
