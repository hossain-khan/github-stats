package dev.hossain.githubstats

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
 * Tests Pull Request stats calculator [PullStats].
 */
@OptIn(ExperimentalCoroutinesApi::class)
internal class PullStatsTest {
    // https://github.com/square/okhttp/tree/master/mockwebserver
    private lateinit var mockWebServer: MockWebServer
    lateinit var pullStats: PullStats

    @BeforeEach
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start(60000)
        Client.baseUrl = mockWebServer.url("/")

        pullStats = PullStats(Client.githubService)
    }

    @AfterEach
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `calculateStats - given pr review - calculates time taken to provide review`() = runTest {
        // Uses data from https://github.com/jquery/jquery/pull/5046
        mockWebServer.enqueue(MockResponse().setBody(respond("pulls-jquery-5046.json")))
        mockWebServer.enqueue(MockResponse().setBody(respond("timeline-jquery-5046.json")))

        val calculateStats = pullStats.calculateStats(123)

        // TODO Test requires update based on return data
        assertThat(calculateStats).isInstanceOf(PullStats.StatsResult.Failure::class.java)
    }

    @Test
    fun `calculateStats - given merged with no reviewer - provides no related metrics`() = runTest {
        // Uses data from https://github.com/square/retrofit/pull/3114
        mockWebServer.enqueue(MockResponse().setBody(respond("pulls-retrofit-3114.json")))
        mockWebServer.enqueue(MockResponse().setBody(respond("timeline-retrofit-3114.json")))

        val calculateStats = pullStats.calculateStats(123)

        // TODO Test requires update based on return data
        assertThat(calculateStats).isInstanceOf(PullStats.StatsResult.Failure::class.java)
    }

    @Test
    fun `calculateStats - given pr was draft - provides time taken after pr was ready for review`() = runTest {
    }

    @Test
    fun `calculateStats - given no assigned reviewer added - provides metrics based on approval event`() = runTest {
    }

    // region: Test Utility Functions
    /** Provides response for given [jsonResponseFile] path in the test resources. */
    private fun respond(jsonResponseFile: String): String {
        return PullStatsTest::class.java.getResource("/$jsonResponseFile")!!.readText()
    }
    // endregion: Test Utility Functions
}
