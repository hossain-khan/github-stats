package dev.hossain.githubstats

import dev.hossain.githubstats.io.Client
import kotlinx.coroutines.runBlocking

fun main(args: Array<String>) {
    println("Hello World!")

    println("Program arguments: ${args.joinToString()}")

    val pullStats = PullStats(Client.githubService)
    runBlocking {
        val pullRequest = Client.githubService.pullRequest(
            BuildConfig.REPO_OWNER,
            BuildConfig.REPO_ID,
            3618
        )
        println(pullRequest)

        val calculateStats = pullStats.calculateStats()
        println(calculateStats)
    }
}
