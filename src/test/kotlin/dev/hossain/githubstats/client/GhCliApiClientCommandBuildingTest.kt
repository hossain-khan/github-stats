package dev.hossain.githubstats.client

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import java.lang.reflect.Method

/**
 * Unit tests for [GhCliApiClient] command building functionality.
 * Tests the internal command building logic to ensure correct gh CLI commands are generated.
 */
class GhCliApiClientCommandBuildingTest {
    private val client = GhCliApiClient()

    /**
     * Uses reflection to access the private buildGhApiCommand method for testing.
     */
    private fun buildGhApiCommand(
        endpoint: String,
        params: Map<String, String>,
    ): List<String> {
        val method: Method =
            GhCliApiClient::class.java.getDeclaredMethod(
                "buildGhApiCommand",
                String::class.java,
                Map::class.java,
            )
        method.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        return method.invoke(client, endpoint, params) as List<String>
    }

    @Test
    fun `buildGhApiCommand - simple endpoint without parameters - generates correct command`() {
        // Arrange
        val endpoint = "/repos/owner/repo/pulls/123"
        val params = emptyMap<String, String>()

        // Act
        val command = buildGhApiCommand(endpoint, params)

        // Assert
        assertThat(command).hasSize(5)
        assertThat(command[0]).isEqualTo("gh")
        assertThat(command[1]).isEqualTo("api")
        assertThat(command[2]).isEqualTo("/repos/owner/repo/pulls/123")
        assertThat(command[3]).isEqualTo("--method")
        assertThat(command[4]).isEqualTo("GET")
    }

    @Test
    fun `buildGhApiCommand - endpoint with single parameter - includes parameter in URL`() {
        // Arrange
        val endpoint = "/repos/owner/repo/pulls"
        val params = mapOf("state" to "closed")

        // Act
        val command = buildGhApiCommand(endpoint, params)

        // Assert
        assertThat(command).hasSize(5)
        assertThat(command[0]).isEqualTo("gh")
        assertThat(command[1]).isEqualTo("api")
        assertThat(command[2]).isEqualTo("/repos/owner/repo/pulls?state=closed")
        assertThat(command[3]).isEqualTo("--method")
        assertThat(command[4]).isEqualTo("GET")
    }

    @Test
    fun `buildGhApiCommand - endpoint with multiple parameters - includes all parameters`() {
        // Arrange
        val endpoint = "/repos/owner/repo/pulls"
        val params =
            mapOf(
                "state" to "closed",
                "page" to "2",
                "per_page" to "50",
            )

        // Act
        val command = buildGhApiCommand(endpoint, params)

        // Assert
        assertThat(command).hasSize(5)
        assertThat(command[0]).isEqualTo("gh")
        assertThat(command[1]).isEqualTo("api")
        // Parameters should be in the query string (order may vary due to map iteration)
        assertThat(command[2]).contains("?")
        assertThat(command[2]).contains("state=closed")
        assertThat(command[2]).contains("page=2")
        assertThat(command[2]).contains("per_page=50")
        assertThat(command[3]).isEqualTo("--method")
        assertThat(command[4]).isEqualTo("GET")
    }

    @Test
    fun `buildGhApiCommand - endpoint with pagination parameters - formats correctly`() {
        // Arrange
        val endpoint = "/repos/owner/repo/issues/123/timeline"
        val params =
            mapOf(
                "page" to "1",
                "per_page" to "100",
            )

        // Act
        val command = buildGhApiCommand(endpoint, params)

        // Assert
        assertThat(command[2]).startsWith("/repos/owner/repo/issues/123/timeline?")
        assertThat(command[2]).contains("page=1")
        assertThat(command[2]).contains("per_page=100")
    }

    @Test
    fun `buildGhApiCommand - search endpoint with complex query - handles search query`() {
        // Arrange
        val endpoint = "/search/issues"
        val params =
            mapOf(
                "q" to "is:pr repo:owner/repo",
                "sort" to "created",
                "order" to "desc",
                "page" to "1",
                "per_page" to "30",
            )

        // Act
        val command = buildGhApiCommand(endpoint, params)

        // Assert
        assertThat(command[2]).startsWith("/search/issues?")
        assertThat(command[2]).contains("q=is:pr repo:owner/repo")
        assertThat(command[2]).contains("sort=created")
        assertThat(command[2]).contains("order=desc")
    }

    @Test
    fun `buildGhApiCommand - contributors endpoint with per_page - includes parameter`() {
        // Arrange
        val endpoint = "/repos/owner/repo/contributors"
        val params = mapOf("per_page" to "10")

        // Act
        val command = buildGhApiCommand(endpoint, params)

        // Assert
        assertThat(command[2]).isEqualTo("/repos/owner/repo/contributors?per_page=10")
    }

    @Test
    fun `buildGhApiCommand - always uses GET method`() {
        // Test multiple different endpoints to ensure GET is always used
        val testCases =
            listOf(
                "/repos/owner/repo/pulls/123" to emptyMap(),
                "/repos/owner/repo/pulls" to mapOf("state" to "open"),
                "/search/issues" to mapOf("q" to "test"),
            )

        testCases.forEach { (endpoint, params) ->
            // Act
            val command = buildGhApiCommand(endpoint, params)

            // Assert
            assertThat(command[3]).isEqualTo("--method")
            assertThat(command[4]).isEqualTo("GET")
        }
    }

    @Test
    fun `buildGhApiCommand - empty parameters - does not add question mark`() {
        // Arrange
        val endpoint = "/repos/owner/repo/pulls/456"
        val params = emptyMap<String, String>()

        // Act
        val command = buildGhApiCommand(endpoint, params)

        // Assert
        assertThat(command[2]).doesNotContain("?")
        assertThat(command[2]).isEqualTo("/repos/owner/repo/pulls/456")
    }

    @Test
    fun `buildGhApiCommand - parameter values with special characters - preserves values`() {
        // Arrange
        val endpoint = "/repos/owner/repo/pulls"
        val params = mapOf("head" to "feature/branch-name")

        // Act
        val command = buildGhApiCommand(endpoint, params)

        // Assert
        assertThat(command[2]).contains("head=feature/branch-name")
    }

    @Test
    fun `buildGhApiCommand - always starts with gh api - correct command structure`() {
        // Arrange
        val endpoint = "/user"
        val params = emptyMap<String, String>()

        // Act
        val command = buildGhApiCommand(endpoint, params)

        // Assert
        assertThat(command[0]).isEqualTo("gh")
        assertThat(command[1]).isEqualTo("api")
    }
}
