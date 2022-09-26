package dev.hossain.githubstats

import dev.hossain.githubstats.formatter.PicnicTableFormatter
import dev.hossain.githubstats.formatter.StatsFormatter
import dev.hossain.githubstats.io.Client.githubService
import kotlinx.coroutines.runBlocking

fun main(args: Array<String>) {
    println("Program arguments: ${args.joinToString()}")

    val pullStats = PullStats(githubService)
    val authorStats = PrAuthorStats(githubService, pullStats)
    val formatter: StatsFormatter = PicnicTableFormatter()
    runBlocking {
        when (val result = pullStats.calculateStats(47550)) {
            is PullStats.StatsResult.Failure -> {
                println("Got error for stats: ${result.error}")
            }
            is PullStats.StatsResult.Success -> {
                println(formatter.formatPrStats(result.stats))
            }
        }

        val prAuthorStats = authorStats.authorStats("hossain-khan")

        prAuthorStats.forEach {
            println(formatter.formatPrStats(it))
        }
    } // end runBlocking
}
