package dev.hossain.githubstats

import dev.hossain.githubstats.formatter.CsvFormatter
import dev.hossain.githubstats.formatter.PicnicTableFormatter
import dev.hossain.githubstats.formatter.StatsFormatter
import dev.hossain.githubstats.io.Client.githubService
import dev.hossain.githubstats.util.LocalProperties
import kotlinx.coroutines.runBlocking

fun main(args: Array<String>) {
    println("Program arguments: ${args.joinToString()}")

    val pullStats = PullStats(githubService)
    val authorStats = PrAuthorStats(githubService, pullStats)
    val formatters: List<StatsFormatter> = listOf(
        PicnicTableFormatter(),
        CsvFormatter()
    )
    val localProperties = LocalProperties()
    val repoOwner: String = localProperties.getRepoOwner()
    val repoId: String = localProperties.getRepoId()
    val prAuthorUserId = "naomi-lgbt"

    println("Getting PR stats for author '$prAuthorUserId' from '$repoId' repository.")

    runBlocking {
        val prAuthorStats = authorStats.authorStats(repoOwner, repoId, prAuthorUserId)

        formatters.forEach {
            println(it.formatAuthorStats(prAuthorStats))
        }
    }
}
