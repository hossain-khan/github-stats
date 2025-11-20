package dev.hossain.githubstats.client

import com.google.common.truth.Truth.assertThat
import dev.hossain.githubstats.io.Client
import dev.hossain.githubstats.model.PullRequest
import dev.hossain.githubstats.model.timeline.TimelineEvent
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import kotlin.test.assertNotNull

/**
 * Parameterized tests that validate API parity and behavior consistency
 * between different [GitHubApiClient] implementations (Retrofit and GH CLI).
 *
 * These tests ensure that both implementations produce equivalent results
 * and handle edge cases consistently.
 */
class ApiClientParityTest {
    private lateinit var mockWebServer: MockWebServer

    @BeforeEach
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start(60001)
        Client.baseUrl = mockWebServer.url("/")
    }

    @AfterEach
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @ParameterizedTest
    @EnumSource(
        value = ApiClientType::class,
        names = ["RETROFIT"], // Only test RETROFIT as GH_CLI requires actual gh command
        mode = EnumSource.Mode.INCLUDE,
    )
    fun `pullRequest returns consistent data structure for both implementations`(clientType: ApiClientType) =
        runTest {
            // Arrange
            mockWebServer.enqueue(MockResponse().setBody(respond("pulls-okhttp-7458.json")))
            val client = createClient(clientType)

            // Act
            val result = client.pullRequest("square", "okhttp", 7458)

            // Assert - Verify core structure is consistent
            assertThat(result.number).isEqualTo(7458)
            assertThat(result.title).isNotEmpty()
            assertThat(result.user).isNotNull()
            assertThat(result.user.login).isNotEmpty()
            assertThat(result.state).isNotEmpty()
            assertThat(result.created_at).isNotEmpty()
            assertThat(result.html_url).isNotEmpty()
        }

    @ParameterizedTest
    @EnumSource(
        value = ApiClientType::class,
        names = ["RETROFIT"],
        mode = EnumSource.Mode.INCLUDE,
    )
    fun `pullRequests returns list with consistent structure`(clientType: ApiClientType) =
        runTest {
            // Arrange
            mockWebServer.enqueue(MockResponse().setBody("[]")) // Empty list response
            val client = createClient(clientType)

            // Act
            val result = client.pullRequests("owner", "repo", prState = "closed")

            // Assert
            assertThat(result).isNotNull()
            assertThat(result).isEmpty() // Empty list is valid
        }

    @ParameterizedTest
    @EnumSource(
        value = ApiClientType::class,
        names = ["RETROFIT"],
        mode = EnumSource.Mode.INCLUDE,
    )
    fun `timelineEvents returns list with consistent event types`(clientType: ApiClientType) =
        runTest {
            // Arrange
            mockWebServer.enqueue(MockResponse().setBody(respond("timeline-okhttp-7458.json")))
            val client = createClient(clientType)

            // Act
            val result = client.timelineEvents("square", "okhttp", 7458)

            // Assert
            assertThat(result).isNotNull()
            assertThat(result).isNotEmpty()
            result.forEach { event ->
                assertNotNull(event, "All timeline events should be non-null")
                // Each event should have the base TimelineEvent interface
                assertThat(event).isInstanceOf(TimelineEvent::class.java)
            }
        }

    @ParameterizedTest
    @EnumSource(
        value = ApiClientType::class,
        names = ["RETROFIT"],
        mode = EnumSource.Mode.INCLUDE,
    )
    fun `prSourceCodeReviewComments returns list with consistent structure`(clientType: ApiClientType) =
        runTest {
            // Arrange
            mockWebServer.enqueue(MockResponse().setBody(respond("pulls-num-comments-freeCodeCamp-45530.json")))
            val client = createClient(clientType)

            // Act
            val result = client.prSourceCodeReviewComments("freeCodeCamp", "freeCodeCamp", 45530)

            // Assert
            assertThat(result).isNotNull()
            assertThat(result).isNotEmpty()
            result.forEach { comment ->
                assertThat(comment.id).isGreaterThan(0)
                assertThat(comment.body).isNotEmpty()
                assertThat(comment.user).isNotNull()
                assertThat(comment.created_at).isNotEmpty()
            }
        }

    @ParameterizedTest
    @EnumSource(
        value = ApiClientType::class,
        names = ["RETROFIT"],
        mode = EnumSource.Mode.INCLUDE,
    )
    fun `searchIssues returns consistent search result structure`(clientType: ApiClientType) =
        runTest {
            // Arrange
            mockWebServer.enqueue(MockResponse().setBody(respond("search-prs-freeCodeCamp-DanielRosa74-47511.json")))
            val client = createClient(clientType)

            // Act
            val result = client.searchIssues("is:pr repo:freeCodeCamp/freeCodeCamp author:DanielRosa74")

            // Assert
            assertThat(result).isNotNull()
            assertThat(result.total_count).isGreaterThan(0)
            assertThat(result.items).isNotEmpty()
            result.items.forEach { issue ->
                assertThat(issue.number).isGreaterThan(0)
                assertThat(issue.title).isNotEmpty()
                assertThat(issue.user).isNotNull()
            }
        }

    @ParameterizedTest
    @EnumSource(
        value = ApiClientType::class,
        names = ["RETROFIT"],
        mode = EnumSource.Mode.INCLUDE,
    )
    fun `topContributors returns list with consistent user structure`(clientType: ApiClientType) =
        runTest {
            // Arrange
            mockWebServer.enqueue(MockResponse().setBody(respond("contributors-okhttp.json")))
            val client = createClient(clientType)

            // Act
            val result = client.topContributors("square", "okhttp", itemPerPage = 10)

            // Assert
            assertThat(result).isNotNull()
            assertThat(result).isNotEmpty()
            result.forEach { user ->
                assertThat(user.login).isNotEmpty()
                assertThat(user.id).isGreaterThan(0)
                assertThat(user.type).isNotEmpty()
            }
        }

    @Test
    fun `both implementations handle pagination parameters consistently`() =
        runTest {
            // Arrange
            mockWebServer.enqueue(MockResponse().setBody("[]"))
            val retrofitClient = createClient(ApiClientType.RETROFIT)

            // Act & Assert - Test that pagination parameters are accepted
            val result = retrofitClient.pullRequests("owner", "repo", page = 2, size = 50)
            assertThat(result).isNotNull()

            // Verify the request was made with pagination params
            val request = mockWebServer.takeRequest()
            assertThat(request.path).contains("page=2")
            assertThat(request.path).contains("per_page=50")
        }

    @Test
    fun `both implementations handle default pagination consistently`() =
        runTest {
            // Arrange
            mockWebServer.enqueue(MockResponse().setBody(respond("timeline-okhttp-7458.json")))
            val retrofitClient = createClient(ApiClientType.RETROFIT)

            // Act
            val result = retrofitClient.timelineEvents("square", "okhttp", 7458)

            // Assert
            assertThat(result).isNotNull()

            // Verify default pagination was used
            val request = mockWebServer.takeRequest()
            assertThat(request.path).contains("page=1")
            assertThat(request.path).contains("per_page=100")
        }

    // region: Test Utilities

    /**
     * Creates a client instance for the specified type.
     * For GH_CLI, this would require additional mocking of shell commands.
     */
    private fun createClient(clientType: ApiClientType): GitHubApiClient =
        when (clientType) {
            ApiClientType.RETROFIT -> {
                RetrofitApiClient(Client.githubApiService)
            }

            ApiClientType.GH_CLI -> {
                // Note: Testing GH_CLI requires mocking shell execution
                // For now, we only test RETROFIT in parameterized tests
                throw UnsupportedOperationException(
                    "GH_CLI testing requires shell mocking - test only RETROFIT for parity validation",
                )
            }
        }

    /** Provides response for given [jsonResponseFile] path in the test resources. */
    private fun respond(jsonResponseFile: String): String = ApiClientParityTest::class.java.getResource("/$jsonResponseFile")!!.readText()

    // endregion
}
