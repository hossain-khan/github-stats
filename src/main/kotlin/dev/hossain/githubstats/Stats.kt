package dev.hossain.githubstats

import dev.hossain.githubstats.io.Client
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.toJavaInstant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

fun main(args: Array<String>) {
    println("Hello World!")

    println("Program arguments: ${args.joinToString()}")

    val dateFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
        .withLocale(Locale.US)
        .withZone(ZoneId.systemDefault())

    val pullStats = PullStats(Client.githubService)
    runBlocking {
        when (val result = pullStats.calculateStats(3613)) {
            is PullStats.StatsResult.Failure -> {
                println("Got error for stats: ${result.error}")
            }

            is PullStats.StatsResult.Success -> {
                println("- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -")
                println("PR: ${result.pullRequest.title} (${result.pullRequest.html_url})")
                println("PR Available: ${dateFormatter.format(result.prReadyOn.toJavaInstant())}")
                println("Review Time: ${result.reviewTime}")
                println(
                    "PR Merged in: ${result.prMergedOn - result.prReadyOn} on ${
                    dateFormatter.format(result.prMergedOn.toJavaInstant())
                    }"
                )
                println("- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -")
            }
        }
    }
}
