package dev.hossain.githubstats.formatter.html

/**
 * Bootstrap-based HTML templates with Chart.js integration for aggregated reports.
 */
object BootstrapTemplate {
    /**
     * Generates a complete Bootstrap-based HTML page with all aggregated charts
     */
    fun aggregatedReport(
        title: String,
        repoId: String,
        dateRange: String,
        sectionsHtml: String,
    ): String =
        """<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>$title</title>
    
    <!-- Bootstrap CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    
    <!-- Chart.js -->
    <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.0/dist/chart.min.js"></script>
    
    <!-- Custom CSS -->
    <style>
        .chart-container {
            position: relative;
            height: 400px;
            margin: 20px 0;
        }
        .section-header {
            border-left: 4px solid #007bff;
            padding-left: 15px;
            margin: 30px 0 20px 0;
        }
        .stats-card {
            box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
            border-radius: 8px;
            margin-bottom: 20px;
        }
        .navbar-brand {
            font-weight: bold;
        }
        .chart-title {
            font-size: 1.1rem;
            font-weight: 600;
            margin-bottom: 15px;
            text-align: center;
        }
    </style>
</head>
<body>
    <!-- Navigation -->
    <nav class="navbar navbar-expand-lg navbar-dark bg-primary">
        <div class="container">
            <a class="navbar-brand" href="#overview">📊 GitHub Stats Report</a>
            <div class="navbar-nav ms-auto">
                <a class="nav-link" href="#overview">Overview</a>
                <a class="nav-link" href="#aggregated">Aggregated</a>
                <a class="nav-link" href="#authors">Authors</a>
                <a class="nav-link" href="#reviewers">Reviewers</a>
            </div>
        </div>
    </nav>

    <div class="container my-4">
        <!-- Header -->
        <section id="overview">
            <div class="row">
                <div class="col-12">
                    <div class="jumbotron bg-light p-4 rounded stats-card">
                        <h1 class="display-6">$title</h1>
                        <p class="lead">Repository: <strong>$repoId</strong></p>
                        <p class="lead">Date Range: <strong>$dateRange</strong></p>
                        <hr class="my-4">
                        <p>This dashboard aggregates all PR statistics and provides comprehensive insights into pull request activity, review patterns, and contributor performance.</p>
                    </div>
                </div>
            </div>
        </section>

        $sectionsHtml
    </div>

    <!-- Bootstrap JS -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>"""

    /**
     * Creates a section for aggregated repository stats
     */
    fun aggregatedSection(chartHtml: String): String =
        """
        <section id="aggregated" class="my-5">
            <div class="section-header">
                <h2>📈 Aggregated Repository Statistics</h2>
                <p class="text-muted">Overall statistics for all contributors in the repository</p>
            </div>
            <div class="row">
                <div class="col-12">
                    <div class="card stats-card">
                        <div class="card-body">
                            $chartHtml
                        </div>
                    </div>
                </div>
            </div>
        </section>
        """

    /**
     * Creates a section for individual author stats
     */
    fun authorsSection(authorsHtml: String): String =
        """
        <section id="authors" class="my-5">
            <div class="section-header">
                <h2>👥 PR Author Statistics</h2>
                <p class="text-muted">Individual statistics for each PR author</p>
            </div>
            <div class="row">
                $authorsHtml
            </div>
        </section>
        """

    /**
     * Creates a section for individual reviewer stats
     */
    fun reviewersSection(reviewersHtml: String): String =
        """
        <section id="reviewers" class="my-5">
            <div class="section-header">
                <h2>🔍 PR Reviewer Statistics</h2>
                <p class="text-muted">Individual statistics for each PR reviewer</p>
            </div>
            <div class="row">
                $reviewersHtml
            </div>
        </section>
        """

    /**
     * Creates a Chart.js bar chart
     */
    fun chartJsBarChart(
        canvasId: String,
        title: String,
        labels: List<String>,
        datasets: List<ChartDataset>,
    ): String {
        val labelsJs = labels.joinToString(",") { "\"$it\"" }
        val datasetsJs = datasets.joinToString(",") { it.toJs() }

        return """
        <div class="chart-title">$title</div>
        <div class="chart-container">
            <canvas id="$canvasId"></canvas>
        </div>
        <script>
        (function() {
            const ctx = document.getElementById('$canvasId').getContext('2d');
            new Chart(ctx, {
                type: 'bar',
                data: {
                    labels: [$labelsJs],
                    datasets: [$datasetsJs]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        title: {
                            display: true,
                            text: '$title'
                        },
                        legend: {
                            display: true,
                            position: 'top'
                        }
                    },
                    scales: {
                        y: {
                            beginAtZero: true
                        }
                    }
                }
            });
        })();
        </script>
        """
    }

    /**
     * Creates a Chart.js pie chart
     */
    fun chartJsPieChart(
        canvasId: String,
        title: String,
        labels: List<String>,
        data: List<Number>,
        backgroundColor: List<String>? = null,
    ): String {
        val labelsJs = labels.joinToString(",") { "\"$it\"" }
        val dataJs = data.joinToString(",")
        val backgroundColorJs =
            backgroundColor?.joinToString(",") { "\"$it\"" }
                ?: generateDefaultColors(data.size).joinToString(",") { "\"$it\"" }

        return """
        <div class="chart-title">$title</div>
        <div class="chart-container">
            <canvas id="$canvasId"></canvas>
        </div>
        <script>
        (function() {
            const ctx = document.getElementById('$canvasId').getContext('2d');
            new Chart(ctx, {
                type: 'pie',
                data: {
                    labels: [$labelsJs],
                    datasets: [{
                        data: [$dataJs],
                        backgroundColor: [$backgroundColorJs],
                        borderWidth: 2
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        title: {
                            display: true,
                            text: '$title'
                        },
                        legend: {
                            display: true,
                            position: 'right'
                        }
                    }
                }
            });
        })();
        </script>
        """
    }

    /**
     * Generates default colors for charts
     */
    private fun generateDefaultColors(count: Int): List<String> {
        val colors =
            listOf(
                "#FF6384",
                "#36A2EB",
                "#FFCE56",
                "#4BC0C0",
                "#9966FF",
                "#FF9F40",
                "#FF6384",
                "#C9CBCF",
                "#4BC0C0",
                "#FF6384",
            )
        return (0 until count).map { colors[it % colors.size] }
    }

    /**
     * Creates a card for individual author/reviewer
     */
    fun statsCard(
        title: String,
        content: String,
    ): String =
        """
        <div class="col-md-6 col-lg-4 mb-4">
            <div class="card stats-card h-100">
                <div class="card-header bg-primary text-white">
                    <h5 class="card-title mb-0">$title</h5>
                </div>
                <div class="card-body">
                    $content
                </div>
            </div>
        </div>
        """
}

/**
 * Represents a Chart.js dataset
 */
data class ChartDataset(
    val label: String,
    val data: List<Number>,
    val backgroundColor: String? = null,
    val borderColor: String? = null,
) {
    fun toJs(): String {
        val dataJs = data.joinToString(",")
        val bg = backgroundColor ?: "#36A2EB"
        val border = borderColor ?: bg

        return """
            {
                label: "$label",
                data: [$dataJs],
                backgroundColor: "$bg",
                borderColor: "$border",
                borderWidth: 1
            }
            """.trimIndent()
    }
}
