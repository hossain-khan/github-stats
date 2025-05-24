package dev.hossain.githubstats.formatter

import dev.hossain.ascii.Art
import dev.hossain.githubstats.AuthorPrStats
import dev.hossain.githubstats.AuthorStats
import dev.hossain.githubstats.PrStats
import dev.hossain.githubstats.ReviewerReviewStats
import dev.hossain.githubstats.formatter.html.Template
import dev.hossain.githubstats.util.AppConfig
import dev.hossain.githubstats.util.FileUtil
import dev.hossain.i18n.Resources
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File
import kotlin.time.DurationUnit

/**
 * Generates HTML based charts for the available data.
 * Currently, it uses [Google Chart](https://developers.google.com/chart) to generate simple charts.
 */
class HtmlChartFormatter(
    private val resources: Resources,
) : StatsFormatter, KoinComponent {
    private val appConfig: AppConfig by inject()

    /**
     * Formats PR review stats for a specific single PR.
     */
    override fun formatSinglePrStats(prStats: PrStats): String {
        val formattedChart = ""

        val prStatsFileName = FileUtil.individualPrReportHtmlChart(prStats)
        File(prStatsFileName).writeText(formattedChart)

        return ""
    }

    /**
     * Formats PR review stats for list of users that reviewed specific user's PRs.
     */
    override fun formatAuthorStats(stats: AuthorStats): String {
        if (stats.reviewStats.isEmpty()) {
            return resources.string("html_error_no_author_stats_to_format", resources.string("art_shrug"))
        }

        val prAuthorId = stats.reviewStats.first().prAuthorId

        // Prepares data for pie chart generation
        // https://developers.google.com/chart/interactive/docs/gallery/piechart
        val statsJsData =
            stats.reviewStats
                .map {
                    "['${it.reviewerId} [${it.stats.size}]', ${it.stats.size}]"
                }.joinToString()

        val chartTitle = resources.string(
            "html_title_reviewer_stats_for_author",
            prAuthorId,
            appConfig.get().repoId,
            appConfig.get().dateLimitAfter,
            appConfig.get().dateLimitBefore,
        )
        val formattedPieChart =
            Template.pieChart(
                title = chartTitle,
                statsJsData = statsJsData,
            )

        val pieChartFileName = FileUtil.authorPieChartHtmlFile(prAuthorId)
        val pieChartFile = File(pieChartFileName)
        pieChartFile.writeText(formattedPieChart)

        // Prepares data for bar chart generation
        // https://developers.google.com/chart/interactive/docs/gallery/barchart
        val barStatsJsData: String =
            listOf(
                "['${resources.string("html_chart_label_reviewer")}', " +
                    "'${resources.string("html_chart_label_total_reviewed")}', " +
                    "'${resources.string("html_chart_label_total_commented")}']",
            ).plus(
                    stats.reviewStats.map {
                        "['${it.reviewerId}', ${it.totalReviews}, ${it.totalComments}]"
                    },
                ).joinToString()

        val formattedBarChart =
            Template.barChart(
                title = chartTitle,
                chartData = barStatsJsData,
                // Multiplied by data columns
                dataSize = stats.reviewStats.size * 2,
            )
        val barChartFileName = FileUtil.authorBarChartHtmlFile(prAuthorId)
        val barChartFile = File(barChartFileName)
        barChartFile.writeText(formattedBarChart)

        // Prepares data for bar chart with author PR's aggregate data generation
        // https://developers.google.com/chart/interactive/docs/gallery/barchart
        @Suppress("ktlint:standard:max-line-length")
        val barStatsJsDataAggregate: String =
            listOf(
                "['${resources.string("html_chart_label_pr_author")}', " +
                    "'${resources.string("html_chart_label_total_prs_created")}', " +
                    "'${resources.string("html_chart_label_total_source_code_review_comments_received")}', " +
                    "'${resources.string("html_chart_label_total_pr_issue_comments_received")}', " +
                    "'${resources.string("html_chart_label_total_pr_review_submissions_received")}']",
            ).plus(
                "['${stats.prStats.authorUserId}', ${stats.prStats.totalPrsCreated}, ${stats.prStats.totalCodeReviewComments},${stats.prStats.totalIssueComments},${stats.prStats.totalPrSubmissionComments}]",
            ).joinToString()

        val formattedBarChartAggregate =
            Template.barChart(
                title = resources.string(
                    "html_title_author_stats",
                    prAuthorId,
                    appConfig.get().repoId,
                    appConfig.get().dateLimitAfter,
                    appConfig.get().dateLimitBefore,
                ),
                chartData = barStatsJsDataAggregate,
                // Multiplied by data columns
                dataSize = 5,
            )
        val barChartFileNameAggregate = FileUtil.authorBarChartAggregateHtmlFile(prAuthorId)
        val barChartFileAggregate = File(barChartFileNameAggregate)
        barChartFileAggregate.writeText(formattedBarChartAggregate)

        val successMessage = resources.string(
            "html_info_charts_written_for_user",
            prAuthorId,
            "\n - file://${pieChartFile.absolutePath}" +
                "\n - file://${barChartFileAggregate.absolutePath}" +
                "\n - file://${barChartFile.absolutePath}",
        )
        return successMessage
    }

    override fun formatAllAuthorStats(aggregatedPrStats: List<AuthorPrStats>): String {
        // Prepares data for bar chart with all author PR's aggregate data generation
        // https://developers.google.com/chart/interactive/docs/gallery/barchart
        @Suppress("ktlint:standard:max-line-length")
        val barStatsJsDataAggregate: String =
            listOf(
                "['${resources.string("html_chart_label_pr_author")}', " +
                    "'${resources.string("html_chart_label_total_prs_created")}', " +
                    "'${resources.string("html_chart_label_total_source_code_review_comments_received")}', " +
                    "'${resources.string("html_chart_label_total_pr_issue_comments_received")}', " +
                    "'${resources.string("html_chart_label_total_pr_review_submissions_received")}']",
            ).plus(
                aggregatedPrStats.filter { it.isEmpty().not() }.map {
                    "['${it.authorUserId}', ${it.totalPrsCreated}, ${it.totalCodeReviewComments},${it.totalIssueComments},${it.totalPrSubmissionComments}]"
                },
            ).joinToString()

        val formattedBarChartAggregate =
            Template.barChart(
                title = resources.string(
                    "html_title_aggregated_pr_stats",
                    appConfig.get().repoId,
                    appConfig.get().dateLimitAfter,
                    appConfig.get().dateLimitBefore,
                ),
                chartData = barStatsJsDataAggregate,
                // Multiplied by data columns
                dataSize = 5,
            )
        val barChartFileNameAggregate = FileUtil.allAuthorBarChartAggregateHtmlFile()
        val barChartFileAggregate = File(barChartFileNameAggregate)
        barChartFileAggregate.writeText(formattedBarChartAggregate)

        return resources.string("html_info_aggregated_chart_written", "\n - ${barChartFileAggregate.toURI()}")
    }

    /**
     * Formats [ReviewerReviewStats] that contains all review stats given by the reviewer.
     */
    override fun formatReviewerStats(stats: ReviewerReviewStats): String {
        if (stats.reviewedPrStats.isEmpty() || stats.reviewedForPrStats.isEmpty()) {
            return resources.string("html_error_no_reviewer_stats_to_format", resources.string("art_shrug"))
        }

        val headerItem: List<String> =
            listOf(
                "[" +
                    "'${resources.string("html_chart_label_reviewed_for_authors")}', " +
                    "'${resources.string("html_chart_label_total_prs_reviewed_by_reviewer_since_date", stats.reviewerId, appConfig.get().dateLimitAfter())}', " +
                    "'${resources.string("html_chart_label_total_code_review_comments")}', " +
                    "'${resources.string("html_chart_label_total_pr_issue_comments")}', " +
                    "'${resources.string("html_chart_label_total_pr_review_comments")}', " +
                    "'${resources.string("html_chart_label_total_all_comments_made")}'" +
                    "]",
            )

        // Prepares data for bar chart generation
        // https://developers.google.com/chart/interactive/docs/gallery/barchart
        val barStatsJsData: String =
            headerItem
                .plus(
                    stats.reviewedForPrStats.map { (prAuthorId, prReviewStats) ->
                        // Get all the comments made by the reviewer for the PR author
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
                title = resources.string("html_title_prs_reviewed_by_reviewer", stats.reviewerId),
                chartData = barStatsJsData,
                // Multiplied by data columns
                dataSize = stats.reviewedForPrStats.size * 6,
            )
        val reviewedForBarChartFileName = FileUtil.prReviewedForCombinedBarChartFilename(stats.reviewerId)
        val reviewedForBarChartFile = File(reviewedForBarChartFileName)
        reviewedForBarChartFile.writeText(formattedBarChart)

        // Prepares data for bar chart generation
        // https://developers.google.com/chart/interactive/docs/gallery/barchart
        val userAllPrChartData: String =
            listOf(
                "" +
                    "[" +
                    "'${resources.string("html_chart_label_pr_number_short")}', " +
                    "'${resources.string("html_chart_label_initial_response_time_mins")}'," +
                    "'${resources.string("html_chart_label_review_time_mins")}'" +
                    "]",
            ).plus(
                stats.reviewedPrStats.map { reviewStats ->
                    "" +
                        "[" +
                        "'${resources.string("html_chart_label_pr_number_short")} ${reviewStats.pullRequest.number}', " +
                        "${reviewStats.initialResponseTime.toInt(DurationUnit.MINUTES)}," +
                        "${reviewStats.reviewCompletion.toInt(DurationUnit.MINUTES)}" +
                        "]"
                },
            ).joinToString()

        val appPrBarChart =
            Template.barChart(
                title = resources.string("html_title_prs_reviewed_by_reviewer", stats.reviewerId),
                chartData = userAllPrChartData,
                dataSize = stats.reviewedPrStats.size,
            )

        val allPrChartFileName = FileUtil.prReviewerReviewedPrStatsBarChartFile(stats.reviewerId)
        val allPrChartFile = File(allPrChartFileName)
        allPrChartFile.writeText(appPrBarChart)

        return resources.string(
            "html_info_charts_written_for_user",
            stats.reviewerId,
            "\n - file://${reviewedForBarChartFile.absolutePath}" +
                "\n - file://${allPrChartFile.absolutePath}",
        )
    }
}
