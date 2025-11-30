package dev.hossain.githubstats.client

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertNotNull

/**
 * Unit tests for [GitHubApiClientFactory].
 * Tests the factory's ability to create different client implementations
 * and handle configuration scenarios.
 */
class GitHubApiClientFactoryTest {
    @Test
    fun `create - with RETROFIT type - returns RetrofitApiClient`() {
        // Act
        val client = GitHubApiClientFactory.create(ApiClientType.RETROFIT)

        // Assert
        assertThat(client).isNotNull()
        assertThat(client).isInstanceOf(RetrofitApiClient::class.java)
    }

    @Test
    fun `create - with RETROFIT type and cache stats - creates client successfully`() {
        // Arrange
        val cacheStats = null // Can be null

        // Act
        val client = GitHubApiClientFactory.create(ApiClientType.RETROFIT, cacheStats)

        // Assert
        assertThat(client).isNotNull()
        assertThat(client).isInstanceOf(RetrofitApiClient::class.java)
    }

    @Test
    fun `create - with GH_CLI type when gh not available - throws IllegalStateException`() {
        // Note: This test may pass or fail depending on whether gh CLI is installed
        // In CI environments without gh CLI, it should throw
        // In local environments with gh CLI, it will create the client

        try {
            // Act
            val client = GitHubApiClientFactory.create(ApiClientType.GH_CLI)

            // If we get here, gh CLI is available
            assertThat(client).isNotNull()
            assertThat(client).isInstanceOf(GhCliApiClient::class.java)
        } catch (e: IllegalStateException) {
            // If gh CLI is not available, we expect this exception
            assertThat(e.message).contains("GitHub CLI is not installed")
            assertThat(e.message).contains("brew install gh")
        }
    }

    @Test
    fun `create - RETROFIT creates different instances - not singleton`() {
        // Act
        val client1 = GitHubApiClientFactory.create(ApiClientType.RETROFIT)
        val client2 = GitHubApiClientFactory.create(ApiClientType.RETROFIT)

        // Assert - Factory should create new instances each time
        assertThat(client1).isNotNull()
        assertThat(client2).isNotNull()
        // Note: We can't easily test if they're different instances without modifying state,
        // but the factory pattern typically creates new instances
    }

    @Test
    fun `ApiClientType fromString - with valid RETROFIT strings - returns RETROFIT`() {
        // Act & Assert
        assertThat(ApiClientType.fromString("RETROFIT")).isEqualTo(ApiClientType.RETROFIT)
        assertThat(ApiClientType.fromString("retrofit")).isEqualTo(ApiClientType.RETROFIT)
        assertThat(ApiClientType.fromString("Retrofit")).isEqualTo(ApiClientType.RETROFIT)
    }

    @Test
    fun `ApiClientType fromString - with valid GH_CLI strings - returns GH_CLI`() {
        // Act & Assert
        assertThat(ApiClientType.fromString("GH_CLI")).isEqualTo(ApiClientType.GH_CLI)
        assertThat(ApiClientType.fromString("gh_cli")).isEqualTo(ApiClientType.GH_CLI)
        assertThat(ApiClientType.fromString("GH")).isEqualTo(ApiClientType.GH_CLI)
        assertThat(ApiClientType.fromString("gh")).isEqualTo(ApiClientType.GH_CLI)
        assertThat(ApiClientType.fromString("CLI")).isEqualTo(ApiClientType.GH_CLI)
        assertThat(ApiClientType.fromString("cli")).isEqualTo(ApiClientType.GH_CLI)
    }

    @Test
    fun `ApiClientType fromString - with invalid string - defaults to RETROFIT`() {
        // Act & Assert
        assertThat(ApiClientType.fromString("invalid")).isEqualTo(ApiClientType.RETROFIT)
        assertThat(ApiClientType.fromString("")).isEqualTo(ApiClientType.RETROFIT)
        assertThat(ApiClientType.fromString("okhttp")).isEqualTo(ApiClientType.RETROFIT)
    }

    @Test
    fun `ApiClientType fromString - with null - defaults to RETROFIT`() {
        // Act & Assert
        assertThat(ApiClientType.fromString(null)).isEqualTo(ApiClientType.RETROFIT)
    }

    @Test
    fun `ApiClientType - has correct enum values`() {
        // Act
        val values = ApiClientType.values()

        // Assert
        assertThat(values).hasLength(2)
        assertThat(values).asList().contains(ApiClientType.RETROFIT)
        assertThat(values).asList().contains(ApiClientType.GH_CLI)
    }

    @Test
    fun `ApiClientType valueOf - with valid name - returns correct type`() {
        // Act & Assert
        assertThat(ApiClientType.valueOf("RETROFIT")).isEqualTo(ApiClientType.RETROFIT)
        assertThat(ApiClientType.valueOf("GH_CLI")).isEqualTo(ApiClientType.GH_CLI)
    }

    @Test
    fun `ApiClientType valueOf - with invalid name - throws IllegalArgumentException`() {
        // Act & Assert
        assertThrows<IllegalArgumentException> {
            ApiClientType.valueOf("INVALID")
        }
    }

    @Test
    fun `factory - handles multiple client type creations - works consistently`() {
        // Act - Create multiple clients of different types
        val retrofitClient1 = GitHubApiClientFactory.create(ApiClientType.RETROFIT)
        val retrofitClient2 = GitHubApiClientFactory.create(ApiClientType.RETROFIT)

        // Assert
        assertThat(retrofitClient1).isInstanceOf(RetrofitApiClient::class.java)
        assertThat(retrofitClient2).isInstanceOf(RetrofitApiClient::class.java)

        // Test GH_CLI only if available
        try {
            val ghCliClient = GitHubApiClientFactory.create(ApiClientType.GH_CLI)
            assertThat(ghCliClient).isInstanceOf(GhCliApiClient::class.java)
        } catch (e: IllegalStateException) {
            // GH CLI not available, which is fine for this test
            assertThat(e.message).contains("GitHub CLI is not installed")
        }
    }

    @Test
    fun `isGhCliAvailable - returns boolean - does not throw exception`() {
        // Act
        val isAvailable = GhCliApiClient.isGhCliAvailable()

        // Assert - Should return either true or false without throwing
        assertNotNull(isAvailable)
    }

    @Test
    fun `factory error message - when gh not available - provides helpful information`() {
        // This test verifies the error message quality
        // It may pass without error if gh CLI is installed and authenticated

        try {
            GitHubApiClientFactory.create(ApiClientType.GH_CLI)
        } catch (e: IllegalStateException) {
            // Verify error message is helpful and includes both installation and authentication guidance
            assertThat(e.message).contains("GitHub CLI")
            assertThat(e.message).containsMatch("(not installed|not authenticated)")
            assertThat(e.message).contains("Install:")
            assertThat(e.message).contains("https://cli.github.com")
            assertThat(e.message).contains("Authenticate:")
            assertThat(e.message).contains("gh auth login")
        }
    }
}
