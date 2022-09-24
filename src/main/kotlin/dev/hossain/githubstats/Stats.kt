package dev.hossain.githubstats

import dev.hossain.githubstats.formatter.PicnicTableFormatter
import dev.hossain.githubstats.formatter.StatsFormatter
import dev.hossain.githubstats.io.Client
import kotlinx.coroutines.runBlocking

fun main(args: Array<String>) {
    println("Program arguments: ${args.joinToString()}")

    val pullStats = PullStats(Client.githubService)
    val formatter: StatsFormatter = PicnicTableFormatter()
    runBlocking {
        // Interesting PRs:
        // https://github.com/square/retrofit/pull/3613
        // https://github.com/square/retrofit/pull/3267
        // https://github.com/freeCodeCamp/freeCodeCamp/pull/47594
        when (val result = pullStats.calculateStats(47594)) {
            is PullStats.StatsResult.Failure -> {
                println("Got error for stats: ${result.error}")
            }
            is PullStats.StatsResult.Success -> {
                println(formatter.formatPrStats(result))
            }
        }
    }
}
