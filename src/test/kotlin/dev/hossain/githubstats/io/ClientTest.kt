package dev.hossain.githubstats.io

import com.google.common.truth.Truth.assertThat
import dev.hossain.githubstats.model.timeline.ClosedEvent
import dev.hossain.githubstats.model.timeline.MergedEvent
import dev.hossain.githubstats.model.timeline.ReviewRequestedEvent
import dev.hossain.githubstats.model.timeline.ReviewedEvent
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
        mockWebServer.enqueue(MockResponse().setBody(respond("timeline-review-requested-event.json")))

        val timelineEvents = Client.githubService.timelineEvents("X", "Y", 1)
        val event = timelineEvents.find { it is ReviewRequestedEvent }
        assertThat(event).isNotNull()

        assertThat((event as ReviewRequestedEvent).created_at).isEqualTo("2022-09-21T14:37:39Z")
    }

    @Test
    fun `given timeline with merged event - parses merged event successfully`() = runTest {
        mockWebServer.enqueue(MockResponse().setBody(respond("timeline-merged-event.json")))

        val timelineEvents = Client.githubService.timelineEvents("X", "Y", 1)

        val event = timelineEvents.find { it is MergedEvent }
        assertThat(event).isNotNull()
        assertThat((event as MergedEvent).created_at).isEqualTo("2021-02-22T07:43:05Z")
    }

    @Test
    fun `given timeline with reviewed event - parses reviewed event successfully`() = runTest {
        mockWebServer.enqueue(MockResponse().setBody(respond("timeline-reviewed-event.json")))

        val timelineEvents = Client.githubService.timelineEvents("X", "Y", 1)

        val event = timelineEvents.find { it is ReviewedEvent }
        assertThat(event).isNotNull()

        assertThat((event as ReviewedEvent).submitted_at).isEqualTo("2022-06-08T02:24:27Z")
    }

    @Test
    fun `given timeline with closed event - parses closed event successfully`() = runTest {
        mockWebServer.enqueue(MockResponse().setBody(respond("timeline-closed-event.json")))

        val timelineEvents = Client.githubService.timelineEvents("X", "Y", 1)

        val event = timelineEvents.find { it is ClosedEvent }
        assertThat(event).isNotNull()

        assertThat((event as ClosedEvent).created_at).isEqualTo("2021-02-22T07:43:05Z")
    }

    @Test
    fun `given multiple timeline events - provides multiple parsed timeline events`() = runTest {
        mockWebServer.enqueue(MockResponse().setBody(respond("timeline-response.json")))

        val timelineEvents = Client.githubService.timelineEvents("X", "Y", 1)
        assertThat(timelineEvents).isNotEmpty()
        assertThat(timelineEvents).hasSize(3)
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