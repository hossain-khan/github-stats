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
        val prAuthorStats = authorStats.authorStats("naomi-lgbt")

        println(formatter.formatAuthorStats(prAuthorStats))
    }
}
