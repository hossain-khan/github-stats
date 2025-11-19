package dev.hossain.githubstats.model.timeline

import com.google.common.truth.Truth.assertThat
import dev.hossain.githubstats.BaseApiMockTest
import dev.hossain.githubstats.client.RetrofitApiClient
import dev.hossain.githubstats.io.Client
import dev.hossain.githubstats.model.User
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Tests [TimelineEvent] and related extension function.
 */
internal class TimelineEventTest : BaseApiMockTest() {
    private lateinit var apiClient: RetrofitApiClient

    @BeforeEach
    fun setUp() {
        apiClient = RetrofitApiClient(Client.githubApiService)
    }

    @Test
    fun `timelineEvents filterTo - given filtered to CommentedEvent - provides filtered items only`() =
        runTest {
            mockWebServer.enqueue(MockResponse().setBody(respond("timeline-okhttp-3873.json")))

            val timelineEvents: List<TimelineEvent> = apiClient.timelineEvents("X", "Y", 1)

            val commentedEvents: List<CommentedEvent> = timelineEvents.filterTo(CommentedEvent::class)
            assertThat(commentedEvents).hasSize(24)
        }

    @Test
    fun `timelineEvents filterTo - given filtered to ReviewedEvent - provides filtered items only`() =
        runTest {
            mockWebServer.enqueue(MockResponse().setBody(respond("timeline-okhttp-3873.json")))

            val timelineEvents: List<TimelineEvent> = apiClient.timelineEvents("X", "Y", 1)

            val commentedEvents: List<ReviewedEvent> = timelineEvents.filterTo(ReviewedEvent::class)
            assertThat(commentedEvents).hasSize(20)
        }

    @Test
    fun `timelineEvents filterTo - given filtered to ClosedEvent - provides filtered items only`() =
        runTest {
            mockWebServer.enqueue(MockResponse().setBody(respond("timeline-okhttp-3873.json")))

            val timelineEvents: List<TimelineEvent> = apiClient.timelineEvents("X", "Y", 1)

            val commentedEvents: List<ClosedEvent> = timelineEvents.filterTo(ClosedEvent::class)
            assertThat(commentedEvents).hasSize(1)
        }

    @Test
    fun `timelineEvents filterTo - given filtered to MergedEvent - provides filtered items only`() =
        runTest {
            mockWebServer.enqueue(MockResponse().setBody(respond("timeline-okhttp-3873.json")))

            val timelineEvents: List<TimelineEvent> = apiClient.timelineEvents("X", "Y", 1)

            val commentedEvents: List<MergedEvent> = timelineEvents.filterTo(MergedEvent::class)
            assertThat(commentedEvents).hasSize(1)
        }

    @Test
    fun `timelineEvents filterTo - given filtered to ReviewedEvent - provides filtered items only with correct data`() =
        runTest {
            mockWebServer.enqueue(MockResponse().setBody(respond("timeline-freeCodeCamp-56555.json")))

            val timelineEvents: List<TimelineEvent> = apiClient.timelineEvents("X", "Y", 1)

            val reviewedEvents: List<ReviewedEvent> = timelineEvents.filterTo(ReviewedEvent::class)
            assertThat(reviewedEvents).hasSize(3)

            // Pick one of the reviewed event and validate all the values in it
            val approvedEvent = reviewedEvents.find { it.state == ReviewedEvent.ReviewState.APPROVED && it.user.login == "naomi-lgbt" }!!
            assertThat(approvedEvent.state).isEqualTo(ReviewedEvent.ReviewState.APPROVED)
            assertThat(approvedEvent.submitted_at).isEqualTo("2024-10-09T16:53:15Z")
            assertThat(approvedEvent.id).isEqualTo(2357705099)
            assertThat(
                approvedEvent.html_url,
            ).isEqualTo("https://github.com/freeCodeCamp/freeCodeCamp/pull/56555#pullrequestreview-2357705099")
            assertThat(approvedEvent.user).isEqualTo(
                User(
                    login = "naomi-lgbt",
                    id = 63889819,
                    avatar_url = "https://avatars.githubusercontent.com/u/63889819?v=4",
                    url = "https://api.github.com/users/naomi-lgbt",
                    html_url = "https://github.com/naomi-lgbt",
                    type = "User",
                    repos_url = "https://api.github.com/users/naomi-lgbt/repos",
                ),
            )
        }
}
