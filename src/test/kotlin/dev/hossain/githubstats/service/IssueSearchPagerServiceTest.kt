package dev.hossain.githubstats.service

import com.google.common.truth.Truth.assertThat
import dev.hossain.githubstats.BaseApiMockTest
import dev.hossain.githubstats.client.RetrofitApiClient
import dev.hossain.githubstats.io.Client
import dev.hossain.githubstats.model.Issue
import dev.hossain.githubstats.util.ErrorProcessor
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith

/**
 * Tests [IssueSearchPagerService] paging.
 */
internal class IssueSearchPagerServiceTest : BaseApiMockTest() {
    private lateinit var issueSearchPager: IssueSearchPagerService

    @BeforeEach
    fun setUp() {
        issueSearchPager =
            IssueSearchPagerService(
                apiClient = RetrofitApiClient(Client.githubApiService),
                errorProcessor = ErrorProcessor(),
            )
    }

    @Test
    fun `searchIssues - responds with error - throws error`() =
        runTest {
            mockWebServer.enqueue(
                MockResponse()
                    .setResponseCode(400)
                    .setBody("{ \"error\": 400 }"),
            )

            assertFailsWith(IllegalStateException::class) {
                issueSearchPager.searchIssues("search-query")
            }
        }

    @Test
    fun `searchIssues - given contains no items does not make next page request`() =
        runTest {
            mockWebServer.enqueue(MockResponse().setBody("{ \"total_count\": 0, \"items\": [] }"))

            val githubIssueResults: List<Issue> = issueSearchPager.searchIssues("search-query")

            assertThat(githubIssueResults).hasSize(0)
        }

    @Test
    fun `searchIssues - given contains less than max per page does not make next page request`() =
        runTest {
            mockWebServer.enqueue(MockResponse().setBody(respond("search-prs-freeCodeCamp-DanielRosa74-47511.json")))

            val githubIssueResults: List<Issue> = issueSearchPager.searchIssues("search-query")

            assertThat(githubIssueResults).hasSize(1)
        }

    @Test
    fun `searchIssues - given contains more than max per page makes next page request`() =
        runTest {
            mockWebServer.enqueue(MockResponse().setBody(respond("search-issue-freeCodeCamp-naomi-lgbt-page-1.json")))
            mockWebServer.enqueue(MockResponse().setBody(respond("search-issue-freeCodeCamp-naomi-lgbt-page-2.json")))
            mockWebServer.enqueue(MockResponse().setBody(respond("search-issue-freeCodeCamp-naomi-lgbt-page-3.json")))

            // Re-initializes the pager with smaller page size to trigger pagination
            issueSearchPager =
                IssueSearchPagerService(
                    apiClient = RetrofitApiClient(Client.githubApiService),
                    errorProcessor = ErrorProcessor(),
                    // Use smaller page size to test pagination with multiple pages
                    pageSize = 10,
                )

            val githubIssueResults: List<Issue> = issueSearchPager.searchIssues("search-query")

            assertThat(githubIssueResults).hasSize(24)
        }
}
