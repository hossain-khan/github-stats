package dev.hossain.githubstats.io

import com.google.common.truth.Truth.assertThat
import dev.hossain.githubstats.model.timeline.PrMergedEvent
import dev.hossain.githubstats.model.timeline.ReviewRequestedEvent
import dev.hossain.githubstats.service.GithubService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

/**
 * Tests [GithubService] APIs.
 */
@OptIn(ExperimentalCoroutinesApi::class)
internal class ClientTest {
    // https://github.com/square/okhttp/tree/master/mockwebserver
    private lateinit var mockWebServer: MockWebServer

    @BeforeEach
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start(60000)
        Client.baseUrl = mockWebServer.url("/")
    }

    @AfterEach
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `given timeline with review_requested event - parses review_requested event successfully`() = runTest {
        mockWebServer.enqueue(MockResponse().setBody(respond("timeline-response.json")))

        val timelineEvents = Client.githubService.timelineEvents("X", "Y", 1)
        assertThat(timelineEvents.find { it is ReviewRequestedEvent }).isNotNull()
    }

    @Test
    fun `given timeline with merged event - parses merged event successfully`() = runTest {
        mockWebServer.enqueue(MockResponse().setBody(respond("timeline-response.json")))

        val timelineEvents = Client.githubService.timelineEvents("X", "Y", 1)

        assertThat(timelineEvents.find { it is PrMergedEvent }).isNotNull()
    }

    @Test
    fun `given empty timeline response - provides empty timeline events`() = runTest {
        mockWebServer.enqueue(MockResponse().setBody("[]"))

        val timelineEvents = Client.githubService.timelineEvents("X", "Y", 1)
        assertEquals(true, timelineEvents.isEmpty())
    }

    // region: Test Utility Functions
    /** Provides response for given [jsonResponseFile] path in the test resources. */
    private fun respond(jsonResponseFile: String): String {
        return ClientTest::class.java.getResource("/$jsonResponseFile")!!.readText()
    }
    // endregion: Test Utility Functions
}
