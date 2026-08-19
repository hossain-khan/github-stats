package dev.hossain.githubstats.formatter.html

import dev.hossain.githubstats.AuthorStats
import dev.hossain.githubstats.ReviewerReviewStats
import dev.hossain.githubstats.UserId
import kotlin.time.DurationUnit

/**
 * Modern HTML templates for standalone Author, Reviewer, and single-chart analytics pages.
 */
object Template {
    /**
     * Generates a complete standalone modern Author Analytics Dashboard.
     */
    fun authorDashboard(
        authorId: UserId,
        repoId: String,
        dateRange: String,
        authorStats: AuthorStats,
    ): String {
        val prStats = authorStats.prStats
        val reviewStats = authorStats.reviewStats
        val totalReviewsReceived = reviewStats.sumOf { it.totalReviews }
        val totalReviewers = reviewStats.size
        val totalCommentsReceived =
            prStats.totalCodeReviewComments +
                prStats.totalIssueComments +
                prStats.totalPrSubmissionComments

        // Reviewer breakdown doughnut chart
        val reviewerLabels = reviewStats.map { it.reviewerId }
        val reviewerReviewCounts = reviewStats.map { it.totalReviews }
        val doughnutChartHtml =
            BootstrapTemplate.chartJsPieChart(
                canvasId = "authorReviewersDoughnut",
                title = "Reviews Received by Reviewer",
                labels = reviewerLabels,
                data = reviewerReviewCounts,
                isDoughnut = true,
            )

        // Reviewer activity bar chart
        val barDatasets =
            listOf(
                ChartDataset(
                    label = "Total Reviews",
                    data = reviewStats.map { it.totalReviews },
                    backgroundColor = "#3b82f6",
                ),
                ChartDataset(
                    label = "Code Review Comments",
                    data = reviewStats.map { it.stats.sumOf { rs -> rs.prComments.codeReviewComment } },
                    backgroundColor = "#8b5cf6",
                ),
            )
        val barChartHtml =
            BootstrapTemplate.chartJsBarChart(
                canvasId = "authorReviewerActivityBar",
                title = "Review & Comment Volume per Reviewer",
                labels = reviewerLabels,
                datasets = barDatasets,
            )

        // Detailed Table Rows
        val tableRowsHtml =
            reviewStats.joinToString("\n") { rStats ->
                val codeComments = rStats.stats.sumOf { it.prComments.codeReviewComment }
                val issueComments = rStats.stats.sumOf { it.prComments.issueComment }
                val submissionComments = rStats.stats.sumOf { it.prComments.prReviewSubmissionComment }

                """
                <tr>
                    <td>
                        <div class="d-flex align-items-center gap-2">
                            <img src="https://github.com/${rStats.reviewerId}.png?size=64" alt="${rStats.reviewerId}" class="user-avatar" onerror="this.src='https://github.githubassets.com/images/modules/logos_page/GitHub-Mark.png'">
                            <div>
                                <a href="https://github.com/${rStats.reviewerId}" target="_blank" class="fw-semibold text-decoration-none text-heading">
                                    ${rStats.reviewerId}
                                </a>
                            </div>
                        </div>
                    </td>
                    <td><span class="badge badge-subtle badge-blue">${rStats.totalReviews}</span></td>
                    <td>$codeComments</td>
                    <td>$issueComments</td>
                    <td>$submissionComments</td>
                    <td><strong>${rStats.totalComments}</strong></td>
                </tr>
                """.trimIndent()
            }

        val kpiCards =
            listOf(
                BootstrapTemplate.kpiCard("PRs Created", "${prStats.totalPrsCreated}", "bi-git", "kpi-icon-blue"),
                BootstrapTemplate.kpiCard("Total Reviews Received", "$totalReviewsReceived", "bi-check2-circle", "kpi-icon-emerald"),
                BootstrapTemplate.kpiCard("Active Reviewers", "$totalReviewers", "bi-people", "kpi-icon-purple"),
                BootstrapTemplate.kpiCard("Comments Received", "$totalCommentsReceived", "bi-chat-left-text", "kpi-icon-amber"),
            )

        return """
<!DOCTYPE html>
<html lang="en" data-bs-theme="light">
<head>
    ${DashboardTheme.headAssets("Author Analytics - $authorId ($repoId)")}
</head>
<body>
    <!-- Top Navigation -->
    <nav class="navbar navbar-expand-lg app-navbar">
        <div class="container-fluid px-lg-4">
            <a class="navbar-brand" href="#">
                <i class="bi bi-person-badge-fill text-primary"></i>
                <span>Author Analytics</span>
                <span class="brand-badge">$authorId</span>
            </a>
            
            <div class="d-flex align-items-center gap-3 ms-auto">
                <div class="d-none d-md-flex align-items-center text-muted small me-2">
                    <i class="bi bi-calendar3 me-2"></i> $dateRange
                </div>
                <button class="btn-theme-toggle" onclick="toggleTheme()" title="Toggle Theme" aria-label="Toggle Theme">
                    <i class="bi bi-moon-stars-fill text-primary" id="themeToggleIcon"></i>
                </button>
            </div>
        </div>
    </nav>

    <main class="container-fluid px-lg-4 py-4">
        <!-- Author Profile & KPI Header -->
        <div class="dash-card p-4 mb-4">
            <div class="d-flex flex-column flex-md-row align-items-md-center justify-content-between gap-3 mb-4">
                <div class="d-flex align-items-center gap-3">
                    <img src="https://github.com/$authorId.png?size=128" alt="$authorId" class="user-avatar-lg" onerror="this.src='https://github.githubassets.com/images/modules/logos_page/GitHub-Mark.png'">
                    <div>
                        <div class="d-flex align-items-center gap-2">
                            <h1 class="h3 fw-bold text-heading mb-0">$authorId</h1>
                            <a href="https://github.com/$authorId" target="_blank" class="badge badge-subtle badge-blue text-decoration-none">
                                <i class="bi bi-github me-1"></i>GitHub Profile
                            </a>
                        </div>
                        <p class="text-muted mb-0">Repository: <strong>$repoId</strong> • Analyzed Period: <strong>$dateRange</strong></p>
                    </div>
                </div>
            </div>

            <!-- KPI Cards Grid -->
            ${BootstrapTemplate.kpiGrid(kpiCards)}
        </div>

        <!-- Charts Row -->
        <div class="row g-4 mb-4">
            <div class="col-12 col-lg-5">
                <div class="dash-card h-100">
                    <div class="card-header-clean">
                        <h2 class="card-title-clean">
                            <i class="bi bi-pie-chart-fill text-primary"></i>
                            <span>Reviewer Distribution</span>
                        </h2>
                    </div>
                    <div class="p-4">
                        $doughnutChartHtml
                    </div>
                </div>
            </div>
            <div class="col-12 col-lg-7">
                <div class="dash-card h-100">
                    <div class="card-header-clean">
                        <h2 class="card-title-clean">
                            <i class="bi bi-bar-chart-fill text-purple"></i>
                            <span>Reviewer Activity & Feedback</span>
                        </h2>
                    </div>
                    <div class="p-4">
                        $barChartHtml
                    </div>
                </div>
            </div>
        </div>

        <!-- Reviewers Table -->
        <div class="dash-card">
            <div class="card-header-clean flex-wrap gap-2">
                <h2 class="card-title-clean">
                    <i class="bi bi-table text-emerald"></i>
                    <span>Reviewers Breakdown</span>
                </h2>
                <div class="search-input-group">
                    <i class="bi bi-search"></i>
                    <input type="text" class="form-control form-control-sm" id="reviewerSearch" onkeyup="filterTable('reviewerSearch', 'authorReviewersTable')" placeholder="Search reviewers...">
                </div>
            </div>
            <div class="table-responsive">
                <table class="table table-modern" id="authorReviewersTable">
                    <thead>
                        <tr>
                            <th>Reviewer</th>
                            <th>Reviews Completed</th>
                            <th>Code Comments</th>
                            <th>Issue Comments</th>
                            <th>Submissions</th>
                            <th>Total Comments</th>
                        </tr>
                    </thead>
                    <tbody>
                        $tableRowsHtml
                    </tbody>
                </table>
            </div>
        </div>
    </main>

    <footer class="dash-footer text-center">
        <div class="container">
            <p class="mb-0">Generated by <strong>GitHub Stats</strong> • Author Analytics for <strong>$authorId</strong></p>
        </div>
    </footer>

    ${DashboardTheme.sharedScripts()}
</body>
</html>
            """.trimIndent()
    }

    /**
     * Generates a complete standalone modern Reviewer Analytics Dashboard.
     */
    fun reviewerDashboard(
        reviewerId: UserId,
        repoId: String,
        dateRange: String,
        reviewerStats: ReviewerReviewStats,
    ): String {
        val totalPrsReviewed = reviewerStats.reviewedPrStats.size
        val totalAuthorsAssisted = reviewerStats.reviewedForPrStats.keys.size
        val totalCommentsMade = reviewerStats.reviewedPrStats.sumOf { it.prComments.allComments }
        val avgInitialResponseMins =
            if (totalPrsReviewed > 0) {
                reviewerStats.reviewedPrStats
                    .map { it.initialResponseTime.toInt(DurationUnit.MINUTES) }
                    .average()
                    .toInt()
            } else {
                0
            }
        val avgReviewTimeMins =
            if (totalPrsReviewed > 0) {
                reviewerStats.reviewedPrStats
                    .map { it.reviewCompletion.toInt(DurationUnit.MINUTES) }
                    .average()
                    .toInt()
            } else {
                0
            }

        // PRs reviewed per author bar chart
        val authorLabels = reviewerStats.reviewedForPrStats.keys.toList()
        val authorPrCounts = reviewerStats.reviewedForPrStats.values.map { it.size }
        val authorBarChartHtml =
            BootstrapTemplate.chartJsBarChart(
                canvasId = "reviewerAuthorBarChart",
                title = "PRs Reviewed by Author",
                labels = authorLabels,
                datasets =
                    listOf(
                        ChartDataset(
                            label = "PRs Reviewed",
                            data = authorPrCounts,
                            backgroundColor = "#10b981",
                        ),
                    ),
            )

        // Turnaround times bar chart (top 15 PRs)
        val prSample = reviewerStats.reviewedPrStats.take(15)
        val prLabels = prSample.map { "PR #${it.pullRequest.number}" }
        val initialResponseData = prSample.map { it.initialResponseTime.toInt(DurationUnit.MINUTES) }
        val completionData = prSample.map { it.reviewCompletion.toInt(DurationUnit.MINUTES) }

        val turnaroundChartHtml =
            BootstrapTemplate.chartJsBarChart(
                canvasId = "reviewerTurnaroundChart",
                title = "Review Turnaround Times (Recent PRs in Minutes)",
                labels = prLabels,
                datasets =
                    listOf(
                        ChartDataset(
                            label = "Initial Response (mins)",
                            data = initialResponseData,
                            backgroundColor = "#f59e0b",
                        ),
                        ChartDataset(
                            label = "Review Completion (mins)",
                            data = completionData,
                            backgroundColor = "#8b5cf6",
                        ),
                    ),
            )

        // Detailed Table Rows
        val tableRowsHtml =
            reviewerStats.reviewedPrStats.joinToString("\n") { prStat ->
                val pr = prStat.pullRequest
                val author = pr.user.login
                val initialRespFormatted = formatDuration(prStat.initialResponseTime.toInt(DurationUnit.MINUTES))
                val reviewCompFormatted = formatDuration(prStat.reviewCompletion.toInt(DurationUnit.MINUTES))
                val prCommentsCount = prStat.prComments.allComments

                """
                <tr>
                    <td>
                        <a href="https://github.com/$repoId/pull/${pr.number}" target="_blank" class="fw-bold text-decoration-none text-primary">
                            #${pr.number}
                        </a>
                        <span class="d-block text-muted small text-truncate" style="max-width: 280px;">${pr.title}</span>
                    </td>
                    <td>
                        <div class="d-flex align-items-center gap-2">
                            <img src="https://github.com/$author.png?size=48" alt="$author" class="user-avatar" onerror="this.src='https://github.githubassets.com/images/modules/logos_page/GitHub-Mark.png'" style="width: 28px; height: 28px;">
                            <span>$author</span>
                        </div>
                    </td>
                    <td><span class="badge badge-subtle badge-amber">$initialRespFormatted</span></td>
                    <td><span class="badge badge-subtle badge-purple">$reviewCompFormatted</span></td>
                    <td><span class="badge badge-subtle badge-blue">$prCommentsCount</span></td>
                    <td>
                        ${if (pr.isMerged) "<span class=\"badge badge-subtle badge-emerald\"><i class=\"bi bi-git me-1\"></i>Merged</span>" else "<span class=\"badge badge-subtle badge-blue\">Closed</span>"}
                    </td>
                </tr>
                """.trimIndent()
            }

        val kpiCards =
            listOf(
                BootstrapTemplate.kpiCard("PRs Reviewed", "$totalPrsReviewed", "bi-check-all", "kpi-icon-emerald"),
                BootstrapTemplate.kpiCard("Authors Supported", "$totalAuthorsAssisted", "bi-people", "kpi-icon-blue"),
                BootstrapTemplate.kpiCard("Comments Written", "$totalCommentsMade", "bi-chat-dots", "kpi-icon-purple"),
                BootstrapTemplate.kpiCard("Avg Response Time", formatDuration(avgInitialResponseMins), "bi-stopwatch", "kpi-icon-amber"),
            )

        return """
<!DOCTYPE html>
<html lang="en" data-bs-theme="light">
<head>
    ${DashboardTheme.headAssets("Reviewer Analytics - $reviewerId ($repoId)")}
</head>
<body>
    <!-- Top Navigation -->
    <nav class="navbar navbar-expand-lg app-navbar">
        <div class="container-fluid px-lg-4">
            <a class="navbar-brand" href="#">
                <i class="bi bi-search-heart-fill text-purple"></i>
                <span>Reviewer Analytics</span>
                <span class="brand-badge">$reviewerId</span>
            </a>
            
            <div class="d-flex align-items-center gap-3 ms-auto">
                <div class="d-none d-md-flex align-items-center text-muted small me-2">
                    <i class="bi bi-calendar3 me-2"></i> $dateRange
                </div>
                <button class="btn-theme-toggle" onclick="toggleTheme()" title="Toggle Theme" aria-label="Toggle Theme">
                    <i class="bi bi-moon-stars-fill text-primary" id="themeToggleIcon"></i>
                </button>
            </div>
        </div>
    </nav>

    <main class="container-fluid px-lg-4 py-4">
        <!-- Reviewer Profile & KPI Header -->
        <div class="dash-card p-4 mb-4">
            <div class="d-flex flex-column flex-md-row align-items-md-center justify-content-between gap-3 mb-4">
                <div class="d-flex align-items-center gap-3">
                    <img src="https://github.com/$reviewerId.png?size=128" alt="$reviewerId" class="user-avatar-lg" onerror="this.src='https://github.githubassets.com/images/modules/logos_page/GitHub-Mark.png'" style="border-color: var(--accent-purple);">
                    <div>
                        <div class="d-flex align-items-center gap-2">
                            <h1 class="h3 fw-bold text-heading mb-0">$reviewerId</h1>
                            <a href="https://github.com/$reviewerId" target="_blank" class="badge badge-subtle badge-purple text-decoration-none">
                                <i class="bi bi-github me-1"></i>GitHub Profile
                            </a>
                        </div>
                        <p class="text-muted mb-0">Repository: <strong>$repoId</strong> • Analyzed Period: <strong>$dateRange</strong></p>
                    </div>
                </div>
            </div>

            <!-- KPI Cards Grid -->
            ${BootstrapTemplate.kpiGrid(kpiCards)}
        </div>

        <!-- Charts Row -->
        <div class="row g-4 mb-4">
            <div class="col-12 col-lg-6">
                <div class="dash-card h-100">
                    <div class="card-header-clean">
                        <h2 class="card-title-clean">
                            <i class="bi bi-bar-chart-fill text-emerald"></i>
                            <span>PRs Reviewed by Author</span>
                        </h2>
                    </div>
                    <div class="p-4">
                        $authorBarChartHtml
                    </div>
                </div>
            </div>
            <div class="col-12 col-lg-6">
                <div class="dash-card h-100">
                    <div class="card-header-clean">
                        <h2 class="card-title-clean">
                            <i class="bi bi-lightning-charge-fill text-amber"></i>
                            <span>Review Turnaround Distribution</span>
                        </h2>
                    </div>
                    <div class="p-4">
                        $turnaroundChartHtml
                    </div>
                </div>
            </div>
        </div>

        <!-- Reviewed PRs Table -->
        <div class="dash-card">
            <div class="card-header-clean flex-wrap gap-2">
                <h2 class="card-title-clean">
                    <i class="bi bi-list-task text-primary"></i>
                    <span>Reviewed Pull Requests</span>
                </h2>
                <div class="search-input-group">
                    <i class="bi bi-search"></i>
                    <input type="text" class="form-control form-control-sm" id="prSearch" onkeyup="filterTable('prSearch', 'reviewedPrsTable')" placeholder="Search PRs or authors...">
                </div>
            </div>
            <div class="table-responsive">
                <table class="table table-modern" id="reviewedPrsTable">
                    <thead>
                        <tr>
                            <th>Pull Request</th>
                            <th>Author</th>
                            <th>Initial Response</th>
                            <th>Review Completion</th>
                            <th>Comments</th>
                            <th>Status</th>
                        </tr>
                    </thead>
                    <tbody>
                        $tableRowsHtml
                    </tbody>
                </table>
            </div>
        </div>
    </main>

    <footer class="dash-footer text-center">
        <div class="container">
            <p class="mb-0">Generated by <strong>GitHub Stats</strong> • Reviewer Analytics for <strong>$reviewerId</strong></p>
        </div>
    </footer>

    ${DashboardTheme.sharedScripts()}
</body>
</html>
            """.trimIndent()
    }

    /**
     * Upgraded standalone pie/doughnut chart page with modern Bootstrap 5 + Chart.js.
     */
    fun pieChart(
        title: String,
        statsJsData: String,
    ): String =
        """
<!DOCTYPE html>
<html lang="en" data-bs-theme="light">
<head>
    ${DashboardTheme.headAssets(title)}
</head>
<body class="p-4">
    <div class="container-fluid">
        <div class="d-flex justify-content-between align-items-center mb-4">
            <h1 class="h4 fw-bold text-heading mb-0">$title</h1>
            <button class="btn-theme-toggle" onclick="toggleTheme()" title="Toggle Theme">
                <i class="bi bi-moon-stars-fill text-primary" id="themeToggleIcon"></i>
            </button>
        </div>
        <div class="dash-card p-4">
            <div class="chart-container-fluid" style="min-height: 480px;">
                <canvas id="standalonePieChart"></canvas>
            </div>
        </div>
    </div>
    <script>
    (function() {
        const rawRows = [$statsJsData];
        const labels = rawRows.map(r => r[0]);
        const data = rawRows.map(r => r[1]);
        const colors = ["#3b82f6", "#8b5cf6", "#10b981", "#f59e0b", "#f43f5e", "#06b6d4", "#6366f1", "#ec4899"];
        
        const ctx = document.getElementById('standalonePieChart').getContext('2d');
        const chart = new Chart(ctx, {
            type: 'doughnut',
            data: {
                labels: labels,
                datasets: [{
                    data: data,
                    backgroundColor: colors,
                    borderWidth: 2,
                    borderRadius: 4
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'right',
                        labels: { usePointStyle: true, padding: 14 }
                    },
                    tooltip: { padding: 12, cornerRadius: 8 }
                },
                cutout: '60%'
            }
        });
        window.activeCharts = window.activeCharts || [];
        window.activeCharts.push(chart);
    })();
    </script>
    ${DashboardTheme.sharedScripts()}
</body>
</html>
        """.trimIndent()

    /**
     * Upgraded standalone bar chart page with modern Bootstrap 5 + Chart.js.
     */
    fun barChart(
        title: String,
        chartData: String,
        dataSize: Int = 10,
    ): String =
        """
<!DOCTYPE html>
<html lang="en" data-bs-theme="light">
<head>
    ${DashboardTheme.headAssets(title)}
</head>
<body class="p-4">
    <div class="container-fluid">
        <div class="d-flex justify-content-between align-items-center mb-4">
            <h1 class="h4 fw-bold text-heading mb-0">$title</h1>
            <button class="btn-theme-toggle" onclick="toggleTheme()" title="Toggle Theme">
                <i class="bi bi-moon-stars-fill text-primary" id="themeToggleIcon"></i>
            </button>
        </div>
        <div class="dash-card p-4">
            <div class="chart-container-fluid" style="min-height: 480px;">
                <canvas id="standaloneBarChart"></canvas>
            </div>
        </div>
    </div>
    <script>
    (function() {
        const rawData = [$chartData];
        if (rawData.length > 1) {
            const headers = rawData[0];
            const rows = rawData.slice(1);
            const labels = rows.map(r => r[0]);
            const colors = ["#3b82f6", "#8b5cf6", "#10b981", "#f59e0b", "#f43f5e", "#06b6d4", "#6366f1"];
            
            const datasets = [];
            for (let col = 1; col < headers.length; col++) {
                datasets.push({
                    label: headers[col],
                    data: rows.map(r => r[col]),
                    backgroundColor: colors[(col - 1) % colors.length],
                    borderRadius: 6
                });
            }

            const ctx = document.getElementById('standaloneBarChart').getContext('2d');
            const chart = new Chart(ctx, {
                type: 'bar',
                data: {
                    labels: labels,
                    datasets: datasets
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: { position: 'top', labels: { usePointStyle: true, padding: 14 } },
                        tooltip: { padding: 12, cornerRadius: 8 }
                    },
                    scales: {
                        y: { beginAtZero: true, grid: { drawBorder: false } },
                        x: { grid: { drawBorder: false } }
                    }
                }
            });
            window.activeCharts = window.activeCharts || [];
            window.activeCharts.push(chart);
        }
    })();
    </script>
    ${DashboardTheme.sharedScripts()}
</body>
</html>
        """.trimIndent()

    private fun formatDuration(minutes: Int): String =
        when {
            minutes <= 0 -> "0m"
            minutes < 60 -> "${minutes}m"
            minutes < 1440 -> "${minutes / 60}h ${minutes % 60}m"
            else -> "${minutes / 1440}d ${(minutes % 1440) / 60}h"
        }
}
