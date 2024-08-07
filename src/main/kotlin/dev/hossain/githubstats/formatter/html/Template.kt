package dev.hossain.githubstats.formatter.html

/**
 * Contains templating function to generate HTML with chart data.
 */
object Template {
    /**
     * Provides HTML content to display pie chart with [statsJsData]
     * @see getPieChartHtml
     * @see pieChartScript
     */
    fun pieChart(
        title: String,
        statsJsData: String,
    ): String = getPieChartHtml(pieChartScript(title, statsJsData))

    /**
     * https://developers.google.com/chart/interactive/docs/gallery/piechart
     * @see pieChart
     */
    private fun getPieChartHtml(chartJsScript: String): String {
        //language=html
        return """
            <html>
              <head>
                <!--Load the AJAX API-->
                <script type="text/javascript" src="https://www.gstatic.com/charts/loader.js"></script>
                <script type="text/javascript">
            $chartJsScript
                </script>
              </head>

              <body>
                <!--Div that will hold the pie chart-->
                <div id="chart_div"></div>
              </body>
            </html>
            """.trimIndent()
    }

    /**
     * https://developers.google.com/chart/interactive/docs/gallery/piechart
     * @see pieChart
     */
    private fun pieChartScript(
        title: String,
        chartRowData: String,
    ): String {
        //language=js
        return """
            // Load the Visualization API and the corechart package.
            google.charts.load('current', {'packages':['corechart']});
            
            // Set a callback to run when the Google Visualization API is loaded.
            google.charts.setOnLoadCallback(drawChart);
            
            // Callback that creates and populates a data table,
            // instantiates the pie chart, passes in the data and
            // draws it.
            function drawChart() {
            
              // Create the data table.
              var data = new google.visualization.DataTable();
              data.addColumn('string', 'Topping');
              data.addColumn('number', 'Slices');
              data.addRows([
                $chartRowData
              ]);
            
              // Set chart options
              var options = {'title':'$title',
                             'width':800,
                             'height':600};
            
              // Instantiate and draw our chart, passing in some options.
              var chart = new google.visualization.PieChart(document.getElementById('chart_div'));
              chart.draw(data, options);
            }
            """.trimIndent()
    }

    /**
     * Provides HTML content to display bar chart with [chartData]
     * @see barChartHtml
     * @see barChartJsScript
     */
    fun barChart(
        title: String,
        chartData: String,
        dataSize: Int,
    ): String = barChartHtml(barChartJsScript(title, chartData), dataSize)

    /**
     * https://developers.google.com/chart/interactive/docs/gallery/barchart
     * @see barChart
     */
    private fun barChartHtml(
        chartJsScript: String,
        dataSize: Int,
    ): String {
        val barChartHeightPercent: Int = if (dataSize > 30) (((dataSize.div(30)) + 1) * 100) else 100

        //language=html
        return """
            <html>
              <head>
                <script type="text/javascript" src="https://www.gstatic.com/charts/loader.js"></script>
                <script type="text/javascript">
            $chartJsScript
                </script>
              </head>
              <body>
                <div id="barchart_material" style="width: 100%; height: $barChartHeightPercent%;"></div>
              </body>
            </html>
            """.trimIndent()
    }

    /**
     * https://developers.google.com/chart/interactive/docs/gallery/barchart
     * @see barChart
     */
    private fun barChartJsScript(
        title: String,
        chartData: String,
    ): String {
        //language=js
        return """
            google.charts.load('current', {'packages':['bar']});
            google.charts.setOnLoadCallback(drawChart);
            
            function drawChart() {
              var data = google.visualization.arrayToDataTable([
                $chartData
              ]);
            
              var options = {
                chart: {
                  title: '$title',
                },
                bars: 'horizontal' // Required for Material Bar Charts.
              };
            
              var chart = new google.charts.Bar(document.getElementById('barchart_material'));
            
              chart.draw(data, google.charts.Bar.convertOptions(options));
            }
            """.trimIndent()
    }
}
