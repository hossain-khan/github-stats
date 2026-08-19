package dev.hossain.githubstats.formatter

import dev.hossain.githubstats.AuthorPrStats
import dev.hossain.githubstats.AuthorStats
import dev.hossain.githubstats.ReviewerReviewStats
import dev.hossain.githubstats.formatter.html.BootstrapTemplate
import dev.hossain.githubstats.formatter.html.ChartDataset
import dev.hossain.githubstats.util.AppConfig
import dev.hossain.githubstats.util.FileUtil
import java.io.File
import kotlin.time.DurationUnit

/**
 * Generates aggregated executive HTML dashboard using Bootstrap 5 and Chart.js.
 * This creates a consolidated admin/analytics dashboard that brings together all PR, author, and reviewer statistics.
 */
class AggregatedHtmlReportGenerator(
    private val appConfig: AppConfig,
) {
    private var aggregatedStats: List<AuthorPrStats> = emptyList()
    private var authorStats: List<AuthorStats> = emptyList()
    private var reviewerStats: List<ReviewerReviewStats> = emptyList()

    /**
     * Collects all the stats data for aggregated report generation.
     */
    fun collectStats(
        aggregatedPrStats: List<AuthorPrStats>,
        allAuthorStats: List<AuthorStats> = emptyList(),
        allReviewerStats: List<ReviewerReviewStats> = emptyList(),
    ) {
        this.aggregatedStats = aggregatedPrStats
        if (allAuthorStats.isNotEmpty()) this.authorStats = allAuthorStats
        if (allReviewerStats.isNotEmpty()) this.reviewerStats = allReviewerStats
    }

    /**
     * Generates the complete aggregated HTML report.
     */
    fun generateAggregatedReport(): String {
        val repoId = appConfig.get().repoId
        val dateRange = "${appConfig.get().dateLimitAfter} to ${appConfig.get().dateLimitBefore}"
        val title = "$repoId Analytics Dashboard"

        // Generate KPI Cards
        val kpiCardsHtml = generateKpiCards()

        // Generate sections
        val aggregatedSectionHtml = generateAggregatedSection()
        val authorsSectionHtml = generateAuthorsSection()
        val reviewersSectionHtml = generateReviewersSection()
        val tablesHtml = generateContributorsTableSection()

        val sectionsHtml =
            listOf(
                aggregatedSectionHtml,
                authorsSectionHtml,
                reviewersSectionHtml,
            ).filter { it.isNotBlank() }.joinToString("\n")

        val htmlContent =
            BootstrapTemplate.aggregatedReport(
                title = title,
                repoId = repoId,
                dateRange = dateRange,
                sectionsHtml = sectionsHtml,
                kpiCardsHtml = kpiCardsHtml,
                tablesHtml = tablesHtml,
            )

        // Write to file
        val outputFile = File(getAggregatedReportFilename())
        outputFile.writeText(htmlContent)

        return "📊 Generated aggregated HTML report at: ${outputFile.toURI()}"
    }

    /**
     * Generates top executive KPI cards.
     */
    private fun generateKpiCards(): String {
        val totalPrs = aggregatedStats.sumOf { it.totalPrsCreated }
        val totalAuthors = aggregatedStats.count { !it.isEmpty() }
        val totalCodeComments = aggregatedStats.sumOf { it.totalCodeReviewComments }
        val totalSubmissions = aggregatedStats.sumOf { it.totalPrSubmissionComments }

        val cards =
            listOf(
                BootstrapTemplate.kpiCard(
                    label = "Total PRs Analyzed",
                    value = "$totalPrs",
                    icon = "bi-git",
                    colorClass = "kpi-icon-blue",
                ),
                BootstrapTemplate.kpiCard(
                    label = "Contributors Tracked",
                    value = "$totalAuthors",
                    icon = "bi-people-fill",
                    colorClass = "kpi-icon-purple",
                ),
                BootstrapTemplate.kpiCard(
                    label = "Code Review Comments",
                    value = "$totalCodeComments",
                    icon = "bi-chat-left-code-fill",
                    colorClass = "kpi-icon-emerald",
                ),
                BootstrapTemplate.kpiCard(
                    label = "Review Submissions",
                    value = "$totalSubmissions",
                    icon = "bi-check2-square",
                    colorClass = "kpi-icon-amber",
                ),
            )

        return BootstrapTemplate.kpiGrid(cards)
    }

    /**
     * Generates the aggregated statistics section with charts.
     */
    private fun generateAggregatedSection(): String {
        if (aggregatedStats.isEmpty()) return ""

        val validStats = aggregatedStats.filter { !it.isEmpty() }
        if (validStats.isEmpty()) return ""

        val labels = validStats.map { it.authorUserId }
        val datasets =
            listOf(
                ChartDataset(
                    label = "PRs Created",
                    data = validStats.map { it.totalPrsCreated },
                    backgroundColor = "#3b82f6",
                ),
                ChartDataset(
                    label = "Code Review Comments",
                    data = validStats.map { it.totalCodeReviewComments },
                    backgroundColor = "#8b5cf6",
                ),
                ChartDataset(
                    label = "Issue Comments",
                    data = validStats.map { it.totalIssueComments },
                    backgroundColor = "#f59e0b",
                ),
                ChartDataset(
                    label = "Review Submissions",
                    data = validStats.map { it.totalPrSubmissionComments },
                    backgroundColor = "#10b981",
                ),
            )

        val chartHtml =
            BootstrapTemplate.chartJsBarChart(
                canvasId = "aggregatedStatsChart",
                title = "PR Creation and Review Activity by Contributor",
                labels = labels,
                datasets = datasets,
            )

        return BootstrapTemplate.aggregatedSection(chartHtml)
    }

    /**
     * Generates the authors statistics section.
     */
    private fun generateAuthorsSection(): String {
        if (authorStats.isEmpty()) return ""

        val authorsHtml =
            authorStats
                .mapIndexed { index, stats ->
                    if (stats.reviewStats.isEmpty()) return@mapIndexed ""

                    val authorId = stats.reviewStats.first().prAuthorId
                    val repoId = appConfig.get().repoId

                    // Create doughnut chart for reviewer distribution
                    val reviewerLabels = stats.reviewStats.map { it.reviewerId }
                    val reviewerData = stats.reviewStats.map { it.stats.size }

                    val pieChartHtml =
                        BootstrapTemplate.chartJsPieChart(
                            canvasId = "authorPieChart$index",
                            title = "Reviewers for $authorId",
                            labels = reviewerLabels,
                            data = reviewerData,
                            isDoughnut = true,
                        )

                    val cardContent =
                        """
                        <div class="d-flex align-items-center gap-3 mb-3">
                            <img src="https://github.com/$authorId.png?size=96" alt="$authorId" class="user-avatar" onerror="this.src='https://github.githubassets.com/images/modules/logos_page/GitHub-Mark.png'">
                            <div>
                                <h4 class="h5 fw-bold text-heading mb-0">$authorId</h4>
                                <span class="badge badge-subtle badge-blue">${stats.prStats.totalPrsCreated} PRs Created</span>
                                <span class="badge badge-subtle badge-purple">${stats.reviewStats.size} Reviewers</span>
                            </div>
                        </div>
                        $pieChartHtml
                        <div class="mt-3 text-end">
                            <a href="REPORTS-$repoId-$authorId/REPORT_-_pr-author-$authorId-pie-chart.html" class="btn btn-sm btn-outline-primary">
                                View Author Hub <i class="bi bi-arrow-right ms-1"></i>
                            </a>
                        </div>
                        """.trimIndent()

                    BootstrapTemplate.statsCard(
                        title = "<i class=\"bi bi-person-fill text-primary me-2\"></i>Author: $authorId",
                        content = cardContent,
                    )
                }.filter { it.isNotBlank() }
                .joinToString("\n")

        return if (authorsHtml.isNotBlank()) {
            BootstrapTemplate.authorsSection(authorsHtml)
        } else {
            ""
        }
    }

    /**
     * Generates the reviewers statistics section.
     */
    private fun generateReviewersSection(): String {
        if (reviewerStats.isEmpty()) return ""

        val reviewersHtml =
            reviewerStats
                .mapIndexed { index, stats ->
                    if (stats.reviewedPrStats.isEmpty() || stats.reviewedForPrStats.isEmpty()) {
                        return@mapIndexed ""
                    }

                    val reviewerId = stats.reviewerId
                    val repoId = appConfig.get().repoId
                    val totalPrsReviewed = stats.reviewedPrStats.size

                    // Create bar chart for reviewed authors
                    val authorLabels = stats.reviewedForPrStats.keys.toList()
                    val authorData = stats.reviewedForPrStats.values.map { it.size }

                    val authorBarChartHtml =
                        BootstrapTemplate.chartJsBarChart(
                            canvasId = "reviewerAuthorChart$index",
                            title = "PRs Reviewed by Author",
                            labels = authorLabels,
                            datasets =
                                listOf(
                                    ChartDataset(
                                        label = "PRs Reviewed",
                                        data = authorData,
                                        backgroundColor = "#10b981",
                                    ),
                                ),
                        )

                    val cardContent =
                        """
                        <div class="d-flex align-items-center gap-3 mb-3">
                            <img src="https://github.com/$reviewerId.png?size=96" alt="$reviewerId" class="user-avatar" onerror="this.src='https://github.githubassets.com/images/modules/logos_page/GitHub-Mark.png'" style="border-color: var(--accent-purple);">
                            <div>
                                <h4 class="h5 fw-bold text-heading mb-0">$reviewerId</h4>
                                <span class="badge badge-subtle badge-purple">$totalPrsReviewed PRs Reviewed</span>
                                <span class="badge badge-subtle badge-emerald">${authorLabels.size} Authors Helped</span>
                            </div>
                        </div>
                        $authorBarChartHtml
                        <div class="mt-3 text-end">
                            <a href="REPORTS-$repoId-$reviewerId/REPORT_-_all-prs-reviewed-by-$reviewerId-bar-chart.html" class="btn btn-sm btn-outline-primary">
                                View Reviewer Hub <i class="bi bi-arrow-right ms-1"></i>
                            </a>
                        </div>
                        """.trimIndent()

                    BootstrapTemplate.statsCard(
                        title = "<i class=\"bi bi-search-heart-fill text-purple me-2\"></i>Reviewer: $reviewerId",
                        content = cardContent,
                    )
                }.filter { it.isNotBlank() }
                .joinToString("\n")

        return if (reviewersHtml.isNotBlank()) {
            BootstrapTemplate.reviewersSection(reviewersHtml)
        } else {
            ""
        }
    }

    /**
     * Generates searchable data table section of all authors and reviewers.
     */
    private fun generateContributorsTableSection(): String {
        val repoId = appConfig.get().repoId
        val validAuthors = aggregatedStats.filter { !it.isEmpty() }
        if (validAuthors.isEmpty()) return ""

        val authorRows =
            validAuthors.joinToString("\n") { author ->
                """
                <tr>
                    <td>
                        <div class="d-flex align-items-center gap-2">
                            <img src="https://github.com/${author.authorUserId}.png?size=48" alt="${author.authorUserId}" class="user-avatar" onerror="this.src='https://github.githubassets.com/images/modules/logos_page/GitHub-Mark.png'" style="width: 32px; height: 32px;">
                            <span class="fw-semibold text-heading">${author.authorUserId}</span>
                        </div>
                    </td>
                    <td><span class="badge badge-subtle badge-blue">${author.totalPrsCreated}</span></td>
                    <td>${author.totalCodeReviewComments}</td>
                    <td>${author.totalIssueComments}</td>
                    <td>${author.totalPrSubmissionComments}</td>
                    <td>
                        <a href="REPORTS-$repoId-${author.authorUserId}/REPORT_-_pr-author-${author.authorUserId}-pie-chart.html" class="btn btn-sm btn-outline-secondary py-0">
                            Details <i class="bi bi-arrow-right"></i>
                        </a>
                    </td>
                </tr>
                """.trimIndent()
            }

        return """
            <section id="contributors-table" class="mb-4">
                <div class="dash-card">
                    <div class="card-header-clean flex-wrap gap-2">
                        <h2 class="card-title-clean">
                            <i class="bi bi-people-fill text-primary"></i>
                            <span>Contributors Summary</span>
                        </h2>
                        <div class="search-input-group">
                            <i class="bi bi-search"></i>
                            <input type="text" class="form-control form-control-sm" id="authorSearch" onkeyup="filterTable('authorSearch', 'authorsTable')" placeholder="Search contributors...">
                        </div>
                    </div>
                    <div class="table-responsive">
                        <table class="table table-modern" id="authorsTable">
                            <thead>
                                <tr>
                                    <th>Contributor</th>
                                    <th>PRs Created</th>
                                    <th>Code Comments</th>
                                    <th>Issue Comments</th>
                                    <th>Review Submissions</th>
                                    <th>Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                $authorRows
                            </tbody>
                        </table>
                    </div>
                </div>
            </section>
            """.trimIndent()
    }

    /**
     * Gets the filename for the aggregated report.
     */
    private fun getAggregatedReportFilename(): String {
        val repoId = appConfig.get().repoId
        return "REPORTS-$repoId-DASHBOARD.html"
    }
}
