package dev.hossain.githubstats.formatter

import dev.hossain.ascii.Art
import dev.hossain.githubstats.AuthorPrStats
import dev.hossain.githubstats.AuthorStats
import dev.hossain.githubstats.PrStats
import dev.hossain.githubstats.ReviewerReviewStats
import dev.hossain.githubstats.formatter.html.Template // Assuming Template is KMP compatible
import dev.hossain.githubstats.util.AppConfig
import dev.hossain.githubstats.util.FileUtil // FileUtil will need refactoring
import dev.hossain.platform.PlatformFile
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.DurationUnit

/**
 * Generates HTML based charts for the available data using [PlatformFile].
 * Currently, it uses [Google Chart](https://developers.google.com/chart) to generate simple charts.
 */
class HtmlChartFormatter :
    StatsFormatter,
    KoinComponent {
    private val appConfig: AppConfig by inject()

    override fun formatSinglePrStats(prStats: PrStats): String {
        val formattedChart = "" // No chart for single PR stats yet

        val prStatsFileName = FileUtil.individualPrReportHtmlChart(prStats)
        PlatformFile(prStatsFileName).writeText(formattedChart)
        // Provide feedback or return path
        return "Written (empty) HTML chart for PR #${prStats.pr.number} to $prStatsFileName"
    }

    override fun formatAuthorStats(stats: AuthorStats): String {
        if (stats.reviewStats.isEmpty()) {
            return "⚠ ERROR: No author stats to format. No charts to generate! ${Art.SHRUG}"
        }

        val prAuthorId = stats.reviewStats.first().prAuthorId

        val statsJsData =
            stats.reviewStats
                .map {
                    "['${it.reviewerId} [${it.stats.size}]', ${it.stats.size}]"
                }.joinToString()

        val chartTitle =
            "PR reviewer`s stats for PRs created by `$prAuthorId` on `${appConfig.get().repoId}` repository " +
                "between ${appConfig.get().dateLimitAfter} and ${appConfig.get().dateLimitBefore}."
        val formattedPieChart =
            Template.pieChart(
                title = chartTitle,
                statsJsData = statsJsData,
            )

        val pieChartFileName = FileUtil.authorPieChartHtmlFile(prAuthorId)
        val pieChartFile = PlatformFile(pieChartFileName)
        pieChartFile.writeText(formattedPieChart)

        val barStatsJsData: String =
            listOf("['Reviewer', 'Total Reviewed', 'Total Commented']")
                .plus(
                    stats.reviewStats.map {
                        "['${it.reviewerId}', ${it.totalReviews}, ${it.totalComments}]"
                    },
                ).joinToString()

        val formattedBarChart =
            Template.barChart(
                title = chartTitle,
                chartData = barStatsJsData,
                dataSize = stats.reviewStats.size * 2,
            )
        val barChartFileName = FileUtil.authorBarChartHtmlFile(prAuthorId)
        val barChartFile = PlatformFile(barChartFileName)
        barChartFile.writeText(formattedBarChart)

        @Suppress("ktlint:standard:max-line-length")
        val barStatsJsDataAggregate: String =
            listOf(
                "['PR Author', 'Total PRs Created', 'Total Source Code Review Comments Received', 'Total PR Issue Comments Received', 'Total PR Review+Re-review Submissions Received']",
            ).plus(
                "['${stats.prStats.authorUserId}', ${stats.prStats.totalPrsCreated}, ${stats.prStats.totalCodeReviewComments},${stats.prStats.totalIssueComments},${stats.prStats.totalPrSubmissionComments}]",
            ).joinToString()

        val formattedBarChartAggregate =
            Template.barChart(
                title =
                    "PR authors`s stats for PRs created by `$prAuthorId` on `${appConfig.get().repoId}` repository " +
                        "between ${appConfig.get().dateLimitAfter} and ${appConfig.get().dateLimitBefore}.",
                chartData = barStatsJsDataAggregate,
                dataSize = 5,
            )
        val barChartFileNameAggregate = FileUtil.authorBarChartAggregateHtmlFile(prAuthorId)
        val barChartFileAggregate = PlatformFile(barChartFileNameAggregate)
        barChartFileAggregate.writeText(formattedBarChartAggregate)

        // Note: file.absolutePath and file.toURI() are JVM specific.
        // For KMP, you'd typically just return the filenames or use a common URI scheme if needed.
        // For now, returning paths that PlatformFile can provide.
        return "📊 Written following charts for user: $prAuthorId. (Copy & paste file path URL in browser to preview)" +
            "\n - ${pieChartFile.getAbsolutePath()}" + // Or just pieChartFileName
            "\n - ${barChartFileAggregate.getAbsolutePath()}" + // Or just barChartFileNameAggregate
            "\n - ${barChartFile.getAbsolutePath()}" // Or just barChartFileName
    }

    override fun formatAllAuthorStats(aggregatedPrStats: List<AuthorPrStats>): String {
        if (aggregatedPrStats.isEmpty()) {
            return "No aggregated stats to format or write."
        }
        @Suppress("ktlint:standard:max-line-length")
        val barStatsJsDataAggregate: String =
            listOf(
                "['PR Author', 'Total PRs Created', 'Total Source Code Review Comments Received', 'Total PR Issue Comments Received', 'Total PR Review+Re-review Submissions Received']",
            ).plus(
                aggregatedPrStats.filter { it.isEmpty().not() }.map {
                    "['${it.authorUserId}', ${it.totalPrsCreated}, ${it.totalCodeReviewComments},${it.totalIssueComments},${it.totalPrSubmissionComments}]"
                },
            ).joinToString()

        val formattedBarChartAggregate =
            Template.barChart(
                title =
                    "Aggregated PR Stats on `${appConfig.get().repoId}` repository " +
                        "between ${appConfig.get().dateLimitAfter} and ${appConfig.get().dateLimitBefore}.",
                chartData = barStatsJsDataAggregate,
                dataSize = 5, // Number of columns in the data table, not rows.
            )
        val barChartFileNameAggregate = FileUtil.allAuthorBarChartAggregateHtmlFile()
        val barChartFileAggregate = PlatformFile(barChartFileNameAggregate)
        barChartFileAggregate.writeText(formattedBarChartAggregate)

        return "📊 Written following aggregated chart for repository: (Copy & paste file path URL in browser to preview)" +
            "\n - ${barChartFileAggregate.getAbsolutePath()}" // Or just barChartFileNameAggregate
    }

    override fun formatReviewerStats(stats: ReviewerReviewStats): String {
        if (stats.reviewedPrStats.isEmpty() || stats.reviewedForPrStats.isEmpty()) {
            return "⚠ ERROR: No reviewer stats to format. No charts to generate! ${Art.SHRUG}"
        }

        val headerItem: List<String> =
            listOf(
                "[" +
                    "'Reviewed For different PR Authors', " +
                    "'Total PRs Reviewed by ${stats.reviewerId} since ${appConfig.get().dateLimitAfter}', " +
                    "'Total Source Code Review Comments', " +
                    "'Total PR Issue Comments', " +
                    "'Total PR Review Comments', " +
                    "'Total All Comments Made'" +
                    "]",
            )

        val barStatsJsData: String =
            headerItem
                .plus(
                    stats.reviewedForPrStats.map { (prAuthorId, prReviewStats) ->
                        val userComments =
                            prReviewStats
                                .map { it.comments.values }
                                .flatten()
                                .filter { it.user == stats.reviewerId }
                        "" +
                            "[" +
                            "'$prAuthorId', " +
                            "${prReviewStats.size}, " +
                            "${userComments.sumOf { it.codeReviewComment }}," +
                            "${userComments.sumOf { it.issueComment }}," +
                            "${userComments.sumOf { it.prReviewSubmissionComment }}," +
                            "${userComments.sumOf { it.allComments }}" +
                            "]"
                    },
                ).joinToString()

        val formattedBarChart =
            Template.barChart(
                title = "PRs Reviewed by ${stats.reviewerId}",
                chartData = barStatsJsData,
                dataSize = stats.reviewedForPrStats.size * 6, // This seems like it should be number of columns.
            )
        val reviewedForBarChartFileName = FileUtil.prReviewedForCombinedBarChartFilename(stats.reviewerId)
        val reviewedForBarChartFile = PlatformFile(reviewedForBarChartFileName)
        reviewedForBarChartFile.writeText(formattedBarChart)

        val userAllPrChartData: String =
            listOf(
                "" +
                    "[" +
                    "'PR#', " +
                    "'Initial Response Time (mins)'," +
                    "'Review Time (mins)'" +
                    "]",
            ).plus(
                stats.reviewedPrStats.map { reviewStats ->
                    "" +
                        "[" +
                        "'PR# ${reviewStats.pullRequest.number}', " +
                        "${reviewStats.initialResponseTime.toInt(DurationUnit.MINUTES)}," +
                        "${reviewStats.reviewCompletion.toInt(DurationUnit.MINUTES)}" +
                        "]"
                },
            ).joinToString()

        val appPrBarChart =
            Template.barChart(
                title = "PRs Reviewed by ${stats.reviewerId}",
                chartData = userAllPrChartData,
                dataSize = stats.reviewedPrStats.size * 3, // This seems like it should be number of columns.
            )

        val allPrChartFileName = FileUtil.prReviewerReviewedPrStatsBarChartFile(stats.reviewerId)
        val allPrChartFile = PlatformFile(allPrChartFileName)
        allPrChartFile.writeText(appPrBarChart)

        return "📊 Written following charts for user: ${stats.reviewerId}. (Copy & paste file path URL in browser to preview)" +
            "\n - ${reviewedForBarChartFile.getAbsolutePath()}" + // Or just reviewedForBarChartFileName
            "\n - ${allPrChartFile.getAbsolutePath()}" // Or just allPrChartFileName
    }
}
