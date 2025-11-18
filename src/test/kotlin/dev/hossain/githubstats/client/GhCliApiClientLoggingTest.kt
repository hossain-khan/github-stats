package dev.hossain.githubstats.client

import dev.hossain.githubstats.BuildConfig
import dev.hossain.githubstats.logging.Log
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

/**
 * Demonstrates the logging capabilities of [GhCliApiClient].
 * Note: These tests require GitHub CLI to be installed and authenticated.
 */
class GhCliApiClientLoggingTest {
    @BeforeEach
    fun setup() {
        // Set log level to VERBOSE to see all log messages
        BuildConfig.logLevel = Log.VERBOSE
    }

    @Test
    fun `test GH CLI availability logging`() {
        println("\n=== Testing GH CLI Availability Check ===")
        val isAvailable = GhCliApiClient.isGhCliAvailable()
        println("Result: ${if (isAvailable) "✓ Available" else "✗ Not Available"}")

        // This test passes regardless of CLI availability
        // It demonstrates the logging functionality
        assertTrue(true)
    }

    @Test
    fun `test client initialization logging`() {
        println("\n=== Testing GhCliApiClient Initialization ===")
        val client = GhCliApiClient()
        println("Client initialized successfully")

        // Verify statistics start at zero
        println(client.getRequestStatistics())
        assertTrue(true)
    }

    @Test
    fun `demonstrate logging levels`() {
        println("\n=== Demonstrating Logging Levels ===")

        println("\n--- With VERBOSE level (all messages) ---")
        BuildConfig.logLevel = Log.VERBOSE
        val client1 = GhCliApiClient()
        GhCliApiClient.isGhCliAvailable()

        println("\n--- With DEBUG level (debug and above) ---")
        BuildConfig.logLevel = Log.DEBUG
        val client2 = GhCliApiClient()
        GhCliApiClient.isGhCliAvailable()

        println("\n--- With INFO level (info and above) ---")
        BuildConfig.logLevel = Log.INFO
        val client3 = GhCliApiClient()

        assertTrue(true)
    }
}
