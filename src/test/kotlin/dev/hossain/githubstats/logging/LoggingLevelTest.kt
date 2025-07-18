package dev.hossain.githubstats.logging

import dev.hossain.githubstats.BuildConfig
import org.junit.jupiter.api.Test

/**
 * Test to demonstrate the logging level changes for database cache.
 */
class LoggingLevelTest {
    @Test
    fun `demonstrate verbose vs debug logging levels`() {
        // Save original log level
        val originalLogLevel = BuildConfig.logLevel

        try {
            println("\n=== Testing with DEBUG level (should show debug but not verbose) ===")
            BuildConfig.logLevel = Log.DEBUG

            Log.v("This VERBOSE message should NOT appear at DEBUG level")
            Log.d("This DEBUG message should appear at DEBUG level")
            Log.i("This INFO message should appear at DEBUG level")

            println("\n=== Testing with VERBOSE level (should show all messages) ===")
            BuildConfig.logLevel = Log.VERBOSE

            Log.v("This VERBOSE message should appear at VERBOSE level")
            Log.d("This DEBUG message should appear at VERBOSE level")
            Log.i("This INFO message should appear at VERBOSE level")

            println("\n=== Testing with INFO level (should show only info and above) ===")
            BuildConfig.logLevel = Log.INFO

            Log.v("This VERBOSE message should NOT appear at INFO level")
            Log.d("This DEBUG message should NOT appear at INFO level")
            Log.i("This INFO message should appear at INFO level")
        } finally {
            // Restore original log level
            BuildConfig.logLevel = originalLogLevel
        }
    }
}
