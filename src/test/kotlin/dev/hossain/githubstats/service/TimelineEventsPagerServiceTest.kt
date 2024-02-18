package dev.hossain.githubstats.service

import com.google.common.truth.Truth.assertThat
import dev.hossain.githubstats.io.Client
import dev.hossain.githubstats.model.timeline.TimelineEvent
import dev.hossain.githubstats.util.ErrorProcessor
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith

/**
 * Tests [TimelineEventsPagerService] paging.
 */
internal class TimelineEventsPagerServiceTest {
    // https://github.com/square/okhttp/tree/master/mockwebserver
    private lateinit var mockWebServer: MockWebServer

    private lateinit var timelinePagerService: TimelineEventsPagerService

    @BeforeEach
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start(60000)
        Client.baseUrl = mockWebServer.url("/")

        timelinePagerService =
            TimelineEventsPagerService(
                githubApiService = Client.githubApiService,
                errorProcessor = ErrorProcessor(),
            )
    }

    @AfterEach
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `getAllTimelineEvents - responds with error - throws error`() =
        runTest {
            mockWebServer.enqueue(
                MockResponse()
                    .setResponseCode(400)
                    .setBody("{ \"error\": 400 }"),
            )

            assertFailsWith(IllegalStateException::class) {
                timelinePagerService.getAllTimelineEvents("X", "Y", 1)
            }
        }

    @Test
    fun `getAllTimelineEvents - given contains no items does not make next page request`() =
        runTest {
            mockWebServer.enqueue(MockResponse().setBody("[]"))

            val timelineEvents: List<TimelineEvent> = timelinePagerService.getAllTimelineEvents("X", "Y", 1)

            assertThat(timelineEvents).hasSize(0)
        }

    @Test
    fun `getAllTimelineEvents - given contains less than max per page does not make next page request`() =
        runTest {
            mockWebServer.enqueue(MockResponse().setBody(respond("timeline-okhttp-3873.json")))

            val timelineEvents: List<TimelineEvent> = timelinePagerService.getAllTimelineEvents("X", "Y", 1)

            assertThat(timelineEvents).hasSize(93)
        }

    @Test
    fun `getAllTimelineEvents - given contains more than max per page makes next page request`() =
        runTest {
            mockWebServer.enqueue(MockResponse().setBody(respond("timeline-jellyfin-2733-page1.json")))
            mockWebServer.enqueue(MockResponse().setBody(respond("timeline-jellyfin-2733-page2.json")))

            val timelineEvents: List<TimelineEvent> = timelinePagerService.getAllTimelineEvents("X", "Y", 1)

            assertThat(timelineEvents).hasSize(195)
        }

    // region: Test Utility Functions

    /** Provides response for given [jsonResponseFile] path in the test resources. */
    private fun respond(jsonResponseFile: String): String {
        return requireNotNull(TimelineEventsPagerServiceTest::class.java.getResource("/$jsonResponseFile")).readText()
    }
    // endregion: Test Utility Functions
}
