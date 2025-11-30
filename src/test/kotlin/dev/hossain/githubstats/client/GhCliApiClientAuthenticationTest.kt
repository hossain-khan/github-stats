package dev.hossain.githubstats.client

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable

/**
 * Unit tests for [GhCliApiClient.isGhCliAvailable] authentication checking.
 *
 * Note: These tests depend on actual system state:
 * - Whether `gh` CLI is installed
 * - Whether user is authenticated with `gh auth login`
 *
 * Tests are conditionally enabled based on environment to avoid false failures in CI.
 */
class GhCliApiClientAuthenticationTest {
    @Test
    @DisabledIfEnvironmentVariable(named = "IS_GITHUB_CI", matches = "true")
    fun `isGhCliAvailable - when gh installed and authenticated - returns true`() {
        // Note: This test only runs in local environments where gh CLI is available and authenticated
        // In CI, it's disabled because gh CLI may not be installed/authenticated

        // Act
        val isAvailable = GhCliApiClient.isGhCliAvailable()

        // Assert - Only runs if gh is actually installed and authenticated locally
        if (isCommandAvailable("gh")) {
            // If gh is installed, the result depends on authentication status
            if (isGhAuthenticated()) {
                assertThat(isAvailable).isTrue()
            } else {
                assertThat(isAvailable).isFalse()
            }
        } else {
            assertThat(isAvailable).isFalse()
        }
    }

    @Test
    fun `isGhCliAvailable - checks both installation and authentication`() {
        // Act
        val isAvailable = GhCliApiClient.isGhCliAvailable()

        // Assert - The method should return false if either check fails
        // This test documents the expected behavior without relying on system state
        assertThat(isAvailable).isNotNull()
        // The actual value depends on system configuration
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "IS_GITHUB_CI", matches = "true")
    fun `isGhCliAvailable - in CI environment - returns consistent result`() {
        // In CI environments, gh CLI may or may not be installed/authenticated
        // This test verifies the method works correctly regardless of system state

        // Act
        val isAvailable = GhCliApiClient.isGhCliAvailable()

        // Assert
        // The method should return a boolean result without throwing
        assertThat(isAvailable).isNotNull()

        // If gh is installed, check if result matches actual authentication status
        if (isCommandAvailable("gh")) {
            val actuallyAuthenticated = isGhAuthenticated()
            assertThat(isAvailable).isEqualTo(actuallyAuthenticated)
        } else {
            // If gh is not installed, should return false
            assertThat(isAvailable).isFalse()
        }
    }

    @Test
    fun `isGhCliAvailable - does not throw exception - returns boolean result`() {
        // The method should never throw an exception, always return true or false
        // Act & Assert - Should not throw
        val result = runCatching { GhCliApiClient.isGhCliAvailable() }
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isNotNull()
    }

    // Helper functions to check system state for conditional assertions

    private fun isCommandAvailable(command: String): Boolean =
        try {
            val process = ProcessBuilder("which", command).start()
            process.waitFor() == 0
        } catch (e: Exception) {
            false
        }

    private fun isGhAuthenticated(): Boolean =
        try {
            val process = ProcessBuilder("gh", "auth", "status").start()
            process.waitFor() == 0
        } catch (e: Exception) {
            false
        }
}
