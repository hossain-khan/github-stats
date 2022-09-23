package dev.hossain.githubstats

import dev.hossain.githubstats.io.Client
import kotlinx.coroutines.runBlocking

fun main(args: Array<String>) {
    println("Hello World!")

    println("Program arguments: ${args.joinToString()}")

    val pullStats = PullStats(Client.githubService)
    runBlocking {
        val calculateStats = pullStats.calculateStats(3613)
        println(calculateStats)
    }
}
