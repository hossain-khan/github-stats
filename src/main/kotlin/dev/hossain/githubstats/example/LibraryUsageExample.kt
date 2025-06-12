package dev.hossain.githubstats.example

import dev.hossain.githubstats.GitHubStatsConfig
import dev.hossain.githubstats.GitHubStatsLibrary
import dev.hossain.githubstats.logging.Log

/**
 * Example demonstrating how to use the GitHub Stats Library in standalone applications.
 */
fun main() {
    // Configure the library
    val config =
        GitHubStatsConfig(
            githubToken = "your_github_token_here",
            repoOwner = "freeCodeCamp",
            repoName = "freeCodeCamp",
            userIds = listOf("naomi-lgbt", "RandellDawson"),
            dateAfter = "2023-01-01",
            dateBefore = "2023-12-31",
            logLevel = Log.INFO,
        )

    // Initialize the library
    val statsLibrary = GitHubStatsLibrary()

    try {
        statsLibrary.initialize(config)

        // Generate both author and reviewer stats
        val results = statsLibrary.generateAllStats()

        println("=== GitHub Stats Generation Results ===")
        results.forEach { result ->
            println(result)
        }
    } catch (e: Exception) {
        println("Error: ${e.message}")
        e.printStackTrace()
    } finally {
        // Clean up
        statsLibrary.shutdown()
    }
}
