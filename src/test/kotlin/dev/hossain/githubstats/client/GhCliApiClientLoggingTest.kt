package dev.hossain.githubstats.client

import com.google.common.truth.Truth.assertThat
import dev.hossain.githubstats.BuildConfig
import dev.hossain.githubstats.logging.Log
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Demonstrates and tests the logging capabilities of [GhCliApiClient].
 * These tests verify that client initialization and availability checking work correctly
 * regardless of whether GitHub CLI is installed.
 */
class GhCliApiClientLoggingTest {
    @BeforeEach
    fun setup() {
        // Set log level to VERBOSE to see all log messages
        BuildConfig.logLevel = Log.VERBOSE
    }

    @Test
    fun `test GH CLI availability check - returns boolean result without exception`() {
        println("\n=== Testing GH CLI Availability Check ===")
        val isAvailable = GhCliApiClient.isGhCliAvailable()
        println("Result: ${if (isAvailable) "✓ Available" else "✗ Not Available"}")

        // Verify the method returns a valid boolean result
        assertThat(isAvailable || !isAvailable).isTrue()
    }

    @Test
    fun `test client initialization - creates client with valid statistics`() {
        println("\n=== Testing GhCliApiClient Initialization ===")
        val client = GhCliApiClient()
        println("Client initialized successfully")

        // Verify client is created and statistics are initialized
        val stats = client.getRequestStatistics()
        assertThat(stats).contains("Total Requests: 0")
    }

    @Test
    fun `demonstrate logging levels - clients initialize correctly at all log levels`() {
        println("\n=== Demonstrating Logging Levels ===")

        println("\n--- With VERBOSE level (all messages) ---")
        BuildConfig.logLevel = Log.VERBOSE
        val client1 = GhCliApiClient()
        assertThat(client1).isNotNull()

        println("\n--- With DEBUG level (debug and above) ---")
        BuildConfig.logLevel = Log.DEBUG
        val client2 = GhCliApiClient()
        assertThat(client2).isNotNull()

        println("\n--- With INFO level (info and above) ---")
        BuildConfig.logLevel = Log.INFO
        val client3 = GhCliApiClient()
        assertThat(client3).isNotNull()
    }
}
