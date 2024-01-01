package dev.hossain.githubstats.service

import com.google.common.truth.Truth.assertThat
import dev.hossain.githubstats.io.Client
import dev.hossain.githubstats.model.Issue
import dev.hossain.githubstats.util.ErrorProcessor
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith

/**
 * Tests [IssueSearchPagerService] paging.
 */
@OptIn(ExperimentalCoroutinesApi::class)
internal class IssueSearchPagerServiceTest {

    // https://github.com/square/okhttp/tree/master/mockwebserver
    private lateinit var mockWebServer: MockWebServer

    private lateinit var issueSearchPager: IssueSearchPagerService

    @BeforeEach
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start(60000)
        Client.baseUrl = mockWebServer.url("/")

        issueSearchPager = IssueSearchPagerService(
            githubApiService = Client.githubApiService,
            errorProcessor = ErrorProcessor()
        )
    }

    @AfterEach
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `searchIssues - responds with error - throws error`() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(400)
                .setBody("{ \"error\": 400 }")
        )

        assertFailsWith(IllegalStateException::class) {
            issueSearchPager.searchIssues("search-query")
        }
    }

    @Test
    fun `searchIssues - given contains no items does not make next page request`() = runTest {
        mockWebServer.enqueue(MockResponse().setBody("{ \"total_count\": 0, \"items\": [] }"))

        val githubIssueResults: List<Issue> = issueSearchPager.searchIssues("search-query")

        assertThat(githubIssueResults).hasSize(0)
    }

    @Test
    fun `searchIssues - given contains less than max per page does not make next page request`() = runTest {
        mockWebServer.enqueue(MockResponse().setBody(respond("search-prs-freeCodeCamp-DanielRosa74-47511.json")))

        val githubIssueResults: List<Issue> = issueSearchPager.searchIssues("search-query")

        assertThat(githubIssueResults).hasSize(1)
    }

    @Test
    fun `searchIssues - given contains more than max per page makes next page request`() = runTest {
        mockWebServer.enqueue(MockResponse().setBody(respond("search-issue-freeCodeCamp-naomi-lgbt-page-1.json")))
        mockWebServer.enqueue(MockResponse().setBody(respond("search-issue-freeCodeCamp-naomi-lgbt-page-2.json")))
        mockWebServer.enqueue(MockResponse().setBody(respond("search-issue-freeCodeCamp-naomi-lgbt-page-3.json")))

        // Re-initializes the pager to reduce page size for testing
        issueSearchPager = IssueSearchPagerService(
            githubApiService = Client.githubApiService,
            errorProcessor = ErrorProcessor(),
            pageSize = 10 // sets the page size low based on unit test
        )

        val githubIssueResults: List<Issue> = issueSearchPager.searchIssues("search-query")

        assertThat(githubIssueResults).hasSize(24)
    }

    // region: Test Utility Functions
    /** Provides response for given [jsonResponseFile] path in the test resources. */
    private fun respond(jsonResponseFile: String): String {
        return requireNotNull(IssueSearchPagerServiceTest::class.java.getResource("/$jsonResponseFile")).readText()
    }
    // endregion: Test Utility Functions
}
