package dev.hossain.githubstats

import dev.hossain.githubstats.BuildConfig.REPO_ID
import dev.hossain.githubstats.BuildConfig.REPO_OWNER
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
        // https://github.com/freeCodeCamp/freeCodeCamp/pull/47550
//        when (val result = pullStats.calculateStats(47550)) {
//            is PullStats.StatsResult.Failure -> {
//                println("Got error for stats: ${result.error}")
//            }
//            is PullStats.StatsResult.Success -> {
//                println(formatter.formatPrStats(result))
//            }
//        }

        val pullRequests = Client.githubService.pullRequests(
            REPO_OWNER, REPO_ID, null,
            "open",
            1,
            2
        )
        println(pullRequests)
    }
}
