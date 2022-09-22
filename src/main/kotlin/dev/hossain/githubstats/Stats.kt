package dev.hossain.githubstats

import dev.hossain.githubstats.io.Client
import kotlinx.coroutines.runBlocking

fun main(args: Array<String>) {
    println("Hello World!")

    println("Program arguments: ${args.joinToString()}")

    runBlocking {
        val listRepos = Client.githubService.listRepos("hossain-khan")
        println(listRepos)
    }
}
