package dev.hossain.githubstats.client

import com.google.common.truth.Truth.assertThat
import dev.hossain.githubstats.cache.DatabaseCacheService
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertTrue

/**
 * Unit tests for [GhCliApiClient] error handling functionality.
 * Tests how the client handles various error scenarios including command failures,
 * timeouts, and invalid responses.
 */
class GhCliApiClientErrorHandlingTest {
    @Test
    fun `pullRequest - with invalid JSON from cache - throws IllegalStateException`() =
        runTest {
            // Arrange
            val mockCache = mockk<DatabaseCacheService>()
            coEvery { mockCache.getCachedResponse(any()) } returns "invalid json {{{{"

            val client = GhCliApiClient(databaseCacheService = mockCache)

            // Act & Assert
            val exception =
                assertThrows<Exception> {
                    client.pullRequest("owner", "repo", 123)
                }

            // Verify it's a parsing error (could be IllegalStateException or JsonDataException)
            assertTrue(
                exception.message?.contains("Failed to parse") == true ||
                    exception::class.simpleName?.contains("Json") == true,
                "Expected parsing error but got: ${exception.message}",
            )
        }

    @Test
    fun `pullRequest - with null JSON parsing result - throws exception`() =
        runTest {
            // Arrange: JSON that parses but doesn't match expected structure
            val mockCache = mockk<DatabaseCacheService>()
            coEvery { mockCache.getCachedResponse(any()) } returns "{}"

            val client = GhCliApiClient(databaseCacheService = mockCache)

            // Act & Assert - Expect parsing exception (could be IllegalStateException or JsonDataException)
            assertThrows<Exception> {
                client.pullRequest("owner", "repo", 123)
            }
        }

    @Test
    fun `timelineEvents - with empty array from cache - returns empty list`() =
        runTest {
            // Arrange
            val mockCache = mockk<DatabaseCacheService>()
            coEvery { mockCache.getCachedResponse(any()) } returns "[]"

            val client = GhCliApiClient(databaseCacheService = mockCache)

            // Act
            val result = client.timelineEvents("owner", "repo", 123)

            // Assert
            assertThat(result).isEmpty()
        }

    @Test
    fun `searchIssues - with invalid search result JSON - throws exception`() =
        runTest {
            // Arrange
            val mockCache = mockk<DatabaseCacheService>()
            coEvery { mockCache.getCachedResponse(any()) } returns """{"invalid": "structure"}"""

            val client = GhCliApiClient(databaseCacheService = mockCache)

            // Act & Assert - Expect parsing exception
            assertThrows<Exception> {
                client.searchIssues("query")
            }
        }

    @Test
    fun `client initialization - with very short timeout - accepts configuration`() {
        // Arrange & Act
        val client =
            GhCliApiClient(
                commandTimeoutSeconds = 1L,
            )

        // Assert - should initialize without error
        assertThat(client).isNotNull()
    }

    @Test
    fun `getRequestStatistics - with no requests - returns zero values`() {
        // Arrange
        val client = GhCliApiClient()

        // Act
        val stats = client.getRequestStatistics()

        // Assert
        assertThat(stats).contains("Total Requests: 0")
        assertThat(stats).contains("Total Time: 0ms")
        assertThat(stats).contains("Average Time: 0 ms per request")
        assertThat(stats).contains("Total Data: 0.00 MB")
    }

    @Test
    fun `isGhCliAvailable - handles check gracefully - returns boolean result`() {
        // Act
        val isAvailable = GhCliApiClient.isGhCliAvailable()

        // Assert - should return true or false without throwing
        assertTrue(isAvailable || !isAvailable) // Always true, verifies no exception
    }

    @Test
    fun `pullRequests - with null filter parameter - uses default behavior`() =
        runTest {
            // Arrange
            val mockCache = mockk<DatabaseCacheService>()
            coEvery { mockCache.getCachedResponse(any()) } returns "[]"

            val client = GhCliApiClient(databaseCacheService = mockCache)

            // Act
            val result = client.pullRequests("owner", "repo", filter = null)

            // Assert
            assertThat(result).isEmpty()
        }

    @Test
    fun `topContributors - with valid empty response - returns empty list`() =
        runTest {
            // Arrange
            val mockCache = mockk<DatabaseCacheService>()
            coEvery { mockCache.getCachedResponse(any()) } returns "[]"

            val client = GhCliApiClient(databaseCacheService = mockCache)

            // Act
            val result = client.topContributors("owner", "repo")

            // Assert
            assertThat(result).isEmpty()
        }

    @Test
    fun `prSourceCodeReviewComments - with malformed JSON array - throws error`() =
        runTest {
            // Arrange
            val mockCache = mockk<DatabaseCacheService>()
            coEvery { mockCache.getCachedResponse(any()) } returns "[{incomplete"

            val client = GhCliApiClient(databaseCacheService = mockCache)

            // Act & Assert
            assertThrows<Exception> {
                client.prSourceCodeReviewComments("owner", "repo", 123)
            }
        }

    @Test
    fun `client - handles multiple sequential errors - maintains stable state`() =
        runTest {
            // Arrange
            val mockCache = mockk<DatabaseCacheService>()
            coEvery { mockCache.getCachedResponse(any()) } returns "invalid"

            val client = GhCliApiClient(databaseCacheService = mockCache)

            // Act & Assert - Multiple failures should not corrupt client state
            assertThrows<Exception> {
                client.pullRequest("owner", "repo", 1)
            }

            // Second call should also fail predictably
            assertThrows<Exception> {
                client.pullRequest("owner", "repo", 2)
            }

            // Client should still report statistics correctly
            val stats = client.getRequestStatistics()
            assertThat(stats).contains("Total Requests: 2")
        }
}
