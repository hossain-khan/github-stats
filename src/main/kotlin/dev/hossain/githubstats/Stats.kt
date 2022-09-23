package dev.hossain.githubstats

import dev.hossain.githubstats.io.Client
import kotlinx.coroutines.runBlocking

fun main(args: Array<String>) {
    println("Hello World!")

    println("Program arguments: ${args.joinToString()}")

    runBlocking {
        val pullRequest = Client.githubService.pullRequest(
            BuildConfig.REPO_OWNER,
            BuildConfig.REPO_ID,
            3618
        )
        println(pullRequest)
    }
}
