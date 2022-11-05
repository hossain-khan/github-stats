package dev.hossain.githubstats.model.timeline

import com.google.common.truth.Truth.assertThat
import dev.hossain.githubstats.io.Client
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Tests [TimelineEvent] and related extension function.
 */
@OptIn(ExperimentalCoroutinesApi::class)
internal class TimelineEventTest {
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
    fun `timelineEvents filterTo - given filtered to CommentedEvent - provides filtered items only`() = runTest {
        mockWebServer.enqueue(MockResponse().setBody(respond("timeline-okhttp-3873.json")))

        val timelineEvents: List<TimelineEvent> = Client.githubApiService.timelineEvents("X", "Y", 1)

        val commentedEvents: List<CommentedEvent> = timelineEvents.filterTo(CommentedEvent::class)
        assertThat(commentedEvents).hasSize(24)
    }

    @Test
    fun `timelineEvents filterTo - given filtered to ReviewedEvent - provides filtered items only`() = runTest {
        mockWebServer.enqueue(MockResponse().setBody(respond("timeline-okhttp-3873.json")))

        val timelineEvents: List<TimelineEvent> = Client.githubApiService.timelineEvents("X", "Y", 1)

        val commentedEvents: List<ReviewedEvent> = timelineEvents.filterTo(ReviewedEvent::class)
        assertThat(commentedEvents).hasSize(20)
    }

    @Test
    fun `timelineEvents filterTo - given filtered to ClosedEvent - provides filtered items only`() = runTest {
        mockWebServer.enqueue(MockResponse().setBody(respond("timeline-okhttp-3873.json")))

        val timelineEvents: List<TimelineEvent> = Client.githubApiService.timelineEvents("X", "Y", 1)

        val commentedEvents: List<ClosedEvent> = timelineEvents.filterTo(ClosedEvent::class)
        assertThat(commentedEvents).hasSize(1)
    }

    @Test
    fun `timelineEvents filterTo - given filtered to MergedEvent - provides filtered items only`() = runTest {
        mockWebServer.enqueue(MockResponse().setBody(respond("timeline-okhttp-3873.json")))

        val timelineEvents: List<TimelineEvent> = Client.githubApiService.timelineEvents("X", "Y", 1)

        val commentedEvents: List<MergedEvent> = timelineEvents.filterTo(MergedEvent::class)
        assertThat(commentedEvents).hasSize(1)
    }

    // region: Test Utility Functions
    /** Provides response for given [jsonResponseFile] path in the test resources. */
    private fun respond(jsonResponseFile: String): String {
        return TimelineEventTest::class.java.getResource("/$jsonResponseFile")!!.readText()
    }
    // endregion: Test Utility Functions
}
