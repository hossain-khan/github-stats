package dev.hossain.githubstats.io

import com.google.common.truth.Truth.assertThat
import dev.hossain.githubstats.model.timeline.ClosedEvent
import dev.hossain.githubstats.model.timeline.CommentedEvent
import dev.hossain.githubstats.model.timeline.MergedEvent
import dev.hossain.githubstats.model.timeline.ReadyForReviewEvent
import dev.hossain.githubstats.model.timeline.ReviewRequestedEvent
import dev.hossain.githubstats.model.timeline.ReviewedEvent
import dev.hossain.githubstats.model.timeline.ReviewedEvent.ReviewState
import dev.hossain.githubstats.service.GithubApiService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

/**
 * Tests [GithubApiService] APIs.
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
        mockWebServer.enqueue(MockResponse().setBody(respond("timeline-event-review-requested.json")))

        val timelineEvents = Client.githubApiService.timelineEvents("X", "Y", 1)
        val event = timelineEvents.find { it is ReviewRequestedEvent }
        assertThat(event).isNotNull()

        assertThat((event as ReviewRequestedEvent).created_at).isEqualTo("2022-09-21T14:37:39Z")
    }

    @Test
    fun `given timeline with review_requested event from team - parses review_requested event successfully`() = runTest {
        mockWebServer.enqueue(MockResponse().setBody(respond("timeline-event-review-requested-team.json")))

        val timelineEvents = Client.githubApiService.timelineEvents("X", "Y", 1)
        val event = timelineEvents.find { it is ReviewRequestedEvent }
        assertThat(event).isNotNull()

        val reviewRequestedEvent = event as ReviewRequestedEvent
        assertThat(reviewRequestedEvent.created_at).isEqualTo("2022-09-19T20:10:38Z")
        assertThat(reviewRequestedEvent.requested_team).isNotNull()
        assertThat(reviewRequestedEvent.requested_team!!.name).isEqualTo("opensearch-core")
        assertThat(reviewRequestedEvent.requested_team!!.slug).isEqualTo("opensearch-core")
    }

    @Test
    fun `given timeline with merged event - parses merged event successfully`() = runTest {
        mockWebServer.enqueue(MockResponse().setBody(respond("timeline-event-merged.json")))

        val timelineEvents = Client.githubApiService.timelineEvents("X", "Y", 1)

        val event = timelineEvents.find { it is MergedEvent }
        assertThat(event).isNotNull()
        assertThat((event as MergedEvent).created_at).isEqualTo("2021-02-22T07:43:05Z")
    }

    @Test
    fun `given timeline with reviewed event - parses reviewed event successfully`() = runTest {
        mockWebServer.enqueue(MockResponse().setBody(respond("timeline-event-reviewed.json")))

        val timelineEvents = Client.githubApiService.timelineEvents("X", "Y", 1)

        val event = timelineEvents.find { it is ReviewedEvent }
        assertThat(event).isNotNull()

        val reviewedEvent = event as ReviewedEvent
        assertThat(reviewedEvent.submitted_at).isEqualTo("2022-06-08T02:24:27Z")
        assertThat(reviewedEvent.state).isEqualTo(ReviewState.APPROVED)
    }

    @Test
    fun `given timeline with closed event - parses closed event successfully`() = runTest {
        mockWebServer.enqueue(MockResponse().setBody(respond("timeline-event-closed.json")))

        val timelineEvents = Client.githubApiService.timelineEvents("X", "Y", 1)

        val event = timelineEvents.find { it is ClosedEvent }
        assertThat(event).isNotNull()

        assertThat((event as ClosedEvent).created_at).isEqualTo("2021-02-22T07:43:05Z")
    }

    @Test
    fun `given timeline with ready_for_review event - parses ready_for_review event successfully`() = runTest {
        mockWebServer.enqueue(MockResponse().setBody(respond("timeline-event-ready-for-review.json")))

        val timelineEvents = Client.githubApiService.timelineEvents("X", "Y", 1)

        val event = timelineEvents.find { it is ReadyForReviewEvent }
        assertThat(event).isNotNull()

        assertThat((event as ReadyForReviewEvent).created_at).isEqualTo("2022-09-21T14:32:13Z")
    }

    @Test
    fun `given timeline with commented event - parses commented event successfully`() = runTest {
        mockWebServer.enqueue(MockResponse().setBody(respond("timeline-event-commented.json")))

        val timelineEvents = Client.githubApiService.timelineEvents("X", "Y", 1)

        val event = timelineEvents.find { it is CommentedEvent }
        assertThat(event).isNotNull()

        val commentedEvent = event as CommentedEvent
        assertThat(commentedEvent.created_at).isEqualTo("2022-10-20T11:42:19Z")
        assertThat(commentedEvent.updated_at).isEqualTo("2022-10-20T12:22:19Z")
        assertThat(commentedEvent.user.login).isEqualTo("ojeytonwilliams")
        assertThat(commentedEvent.actor.login).isEqualTo("ojeytonwilliams")
    }

    @Test
    fun `given multiple timeline events - provides multiple parsed timeline events`() = runTest {
        mockWebServer.enqueue(MockResponse().setBody(respond("timeline-response.json")))

        val timelineEvents = Client.githubApiService.timelineEvents("X", "Y", 1)
        assertThat(timelineEvents).isNotEmpty()
        assertThat(timelineEvents).hasSize(3)
    }

    @Test
    fun `given empty timeline response - provides empty timeline events`() = runTest {
        mockWebServer.enqueue(MockResponse().setBody("[]"))

        val timelineEvents = Client.githubApiService.timelineEvents("X", "Y", 1)
        assertEquals(true, timelineEvents.isEmpty())
    }

    @Test
    fun `given pull request response - provides parsed PR data`() = runTest {
        mockWebServer.enqueue(MockResponse().setBody(respond("pulls-number.json")))

        val pullRequest = Client.githubApiService.pullRequest("X", "Y", 1)

        assertThat(pullRequest.created_at).isEqualTo("2021-08-18T15:23:51Z")
    }

    @Test
    fun `given pull request comments - provides parsed PR comments`() = runTest {
        mockWebServer.enqueue(MockResponse().setBody(respond("pulls-num-comments-freeCodeCamp-45530.json")))

        val comments = Client.githubApiService.prSourceCodeReviewComments("X", "Y", 1)

        assertThat(comments.size).isEqualTo(4)
    }

    // region: Test Utility Functions
    /** Provides response for given [jsonResponseFile] path in the test resources. */
    private fun respond(jsonResponseFile: String): String {
        return ClientTest::class.java.getResource("/$jsonResponseFile")!!.readText()
    }
    // endregion: Test Utility Functions
}
