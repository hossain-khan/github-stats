package dev.hossain.githubstats

import com.google.common.truth.Truth.assertThat
import dev.hossain.githubstats.io.Client
import dev.hossain.githubstats.repository.PullRequestStatsRepo.StatsResult
import dev.hossain.githubstats.repository.PullRequestStatsRepoImpl
import dev.hossain.time.UserTimeZone
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.time.Duration

/**
 * Tests Pull Request stats calculator [PullRequestStatsRepoImpl].
 */
@OptIn(ExperimentalCoroutinesApi::class)
internal class PullRequestStatsRepoTest {
    // https://github.com/square/okhttp/tree/master/mockwebserver
    private lateinit var mockWebServer: MockWebServer
    lateinit var pullRequestStatsRepo: PullRequestStatsRepoImpl

    private companion object {
        const val REPO_OWNER = "owner"
        const val REPO_ID = "repository-name"
    }

    @BeforeEach
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start(60000)
        Client.baseUrl = mockWebServer.url("/")

        pullRequestStatsRepo = PullRequestStatsRepoImpl(Client.githubApiService, UserTimeZone())
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
        mockWebServer.enqueue(MockResponse().setBody("[]")) // PR Review comments

        val calculateStats = pullRequestStatsRepo.stats(REPO_OWNER, REPO_ID, 123)

        assertThat(calculateStats).isInstanceOf(StatsResult.Success::class.java)
    }

    @Test
    fun `calculateStats - given many pr comments and review - calculates only the approval time`() = runTest {
        // Uses data from https://github.com/opensearch-project/OpenSearch/pull/4515
        mockWebServer.enqueue(MockResponse().setBody(respond("pulls-opensearch-4515.json")))
        mockWebServer.enqueue(MockResponse().setBody(respond("timeline-opensearch-4515.json")))
        mockWebServer.enqueue(MockResponse().setBody("[]")) // PR Review comments

        val statsResult = pullRequestStatsRepo.stats(REPO_OWNER, REPO_ID, 123)

        assertThat(statsResult).isInstanceOf(StatsResult.Success::class.java)
    }

    @Test
    fun `calculateStats - given reviewer was added later - provides time after reviewer was added`() = runTest {
        // Uses data from https://github.com/freeCodeCamp/freeCodeCamp/pull/47594
        // User `naomi-lgbt` was added later in the PR.
        // This is also interesting PR because changes was requested (See issue #51)

        // Uses data from https://github.com/opensearch-project/OpenSearch/pull/4515
        mockWebServer.enqueue(MockResponse().setBody(respond("pulls-freeCodeCamp-47594.json")))
        mockWebServer.enqueue(MockResponse().setBody(respond("timeline-freeCodeCamp-47594.json")))
        mockWebServer.enqueue(MockResponse().setBody("[]")) // PR Review comments

        val statsResult = pullRequestStatsRepo.stats(REPO_OWNER, REPO_ID, 123)

        assertThat(statsResult).isInstanceOf(StatsResult.Success::class.java)

        val result = statsResult as StatsResult.Success
        val reviewTime = result.stats.reviewTime["naomi-lgbt"]
        assertThat(reviewTime).isLessThan(Duration.parse("8h"))
    }

    @Test
    fun `calculateStats - given multiple reviews and dismissed reviews - provides stats accordingly`() = runTest {
        // Uses data from https://github.com/freeCodeCamp/freeCodeCamp/pull/47550
        // A lot of review comments, 2 people approved after dismissal

        // Uses data from https://github.com/opensearch-project/OpenSearch/pull/4515
        mockWebServer.enqueue(MockResponse().setBody(respond("pulls-freeCodeCamp-47550.json")))
        mockWebServer.enqueue(MockResponse().setBody(respond("timeline-freeCodeCamp-47550.json")))
        mockWebServer.enqueue(MockResponse().setBody("[]")) // PR Review comments

        val statsResult = pullRequestStatsRepo.stats(REPO_OWNER, REPO_ID, 123)

        assertThat(statsResult).isInstanceOf(StatsResult.Success::class.java)

        val result = statsResult as StatsResult.Success
        val reviewTime = result.stats.reviewTime
        assertThat(reviewTime).hasSize(2)
    }

    @Test
    fun `calculateStats - pr creator commented on PR - does not contain review metrics for pr creator`() = runTest {
        // Uses data from https://github.com/square/retrofit/pull/3267
        mockWebServer.enqueue(MockResponse().setBody(respond("pulls-retrofit-3267.json")))
        mockWebServer.enqueue(MockResponse().setBody(respond("timeline-retrofit-3267.json")))
        mockWebServer.enqueue(MockResponse().setBody("[]")) // PR Review comments

        val statsResult = pullRequestStatsRepo.stats(REPO_OWNER, REPO_ID, 123)

        assertThat(statsResult).isInstanceOf(StatsResult.Success::class.java)

        val result = statsResult as StatsResult.Success
        assertThat(result.stats.reviewTime)
            .doesNotContainKey("JakeWharton")
    }

    @Test
    fun `calculateStats - given merged with no reviewer - provides no related metrics`() = runTest {
        // Uses data from https://github.com/hossain-khan/github-stats/pull/27
        mockWebServer.enqueue(MockResponse().setBody(respond("pulls-githubstats-27.json")))
        mockWebServer.enqueue(MockResponse().setBody(respond("timeline-githubstats-27.json")))
        mockWebServer.enqueue(MockResponse().setBody("[]")) // PR Review comments

        val calculateStats = pullRequestStatsRepo.stats(REPO_OWNER, REPO_ID, 123)

        assertThat(calculateStats).isInstanceOf(StatsResult.Success::class.java)
    }

    @Test
    fun `calculateStats - given pr was draft - provides time taken after pr was ready for review`() = runTest {
    }

    @Test
    fun `calculateStats - given no assigned reviewer added - provides metrics based on approval event`() = runTest {
        // Uses data from https://github.com/square/retrofit/pull/3114
        mockWebServer.enqueue(MockResponse().setBody(respond("pulls-retrofit-3114.json")))
        mockWebServer.enqueue(MockResponse().setBody(respond("timeline-retrofit-3114.json")))
        mockWebServer.enqueue(MockResponse().setBody("[]")) // PR Review comments

        val calculateStats = pullRequestStatsRepo.stats(REPO_OWNER, REPO_ID, 123)

        assertThat(calculateStats).isInstanceOf(StatsResult.Success::class.java)
    }

    @Test
    fun `calculateStats - given pr reviewed in time - provides correct review time`() = runTest {
        mockWebServer.enqueue(MockResponse().setBody(respond("pulls-freeCodeCamp-47511.json")))
        mockWebServer.enqueue(MockResponse().setBody(respond("timeline-freeCodeCamp-45711.json")))
        mockWebServer.enqueue(MockResponse().setBody("[]")) // PR Review comments

        val statsResult = pullRequestStatsRepo.stats(REPO_OWNER, REPO_ID, 123)
        val result = statsResult as StatsResult.Success
        assertThat(result.stats.reviewTime).hasSize(2)
        assertThat(result.stats.reviewTime["DanielRosa74"]).isEqualTo(Duration.parse("7m"))
    }

    @Test
    fun `calculateStats - given pr has multiple issue comments - provides comment count for each user`() = runTest {
        // Lots of comments by different users
        // Uses data from https://github.com/square/okhttp/pull/3873
        mockWebServer.enqueue(MockResponse().setBody(respond("pulls-okhttp-3873.json")))
        mockWebServer.enqueue(MockResponse().setBody(respond("timeline-okhttp-3873.json")))
        mockWebServer.enqueue(MockResponse().setBody("[]")) // PR Review comments

        val statsResult = pullRequestStatsRepo.stats(REPO_OWNER, REPO_ID, 123)
        val stats = (statsResult as StatsResult.Success).stats
        assertThat(stats.comments).hasSize(5)
        assertThat(stats.comments.keys)
            .containsExactlyElementsIn(setOf("swankjesse", "jjshanks", "yschimke", "mjpitz", "JakeWharton"))
        assertThat(stats.comments["swankjesse"]!!.issueComment).isEqualTo(3)
    }

    @Test
    fun `calculateStats - given pr has multiple issue and review comments - provides all comment count for each user`() = runTest {
        // Lots of comments by different users
        // Uses data from https://github.com/square/okhttp/pull/3873
        mockWebServer.enqueue(MockResponse().setBody(respond("pulls-okhttp-3873.json")))
        mockWebServer.enqueue(MockResponse().setBody(respond("timeline-okhttp-3873.json")))
        mockWebServer.enqueue(MockResponse().setBody(respond("pulls-num-comments-okhttp-3873.json")))

        val statsResult = pullRequestStatsRepo.stats(REPO_OWNER, REPO_ID, 123)
        val stats = (statsResult as StatsResult.Success).stats
        assertThat(stats.comments).hasSize(5)
        assertThat(stats.comments.keys)
            .containsExactlyElementsIn(setOf("swankjesse", "jjshanks", "yschimke", "mjpitz", "JakeWharton"))

        val userPrComment: UserPrComment = stats.comments["yschimke"]!!

        // yschimke made 9 PR comment and 21 review comment. Total: 30 comments.
        assertThat(userPrComment.issueComment).isEqualTo(9)
        assertThat(userPrComment.reviewComment).isEqualTo(21)
        assertThat(userPrComment.allComments).isEqualTo(30)
    }

    // region: Test Utility Functions
    /** Provides response for given [jsonResponseFile] path in the test resources. */
    private fun respond(jsonResponseFile: String): String {
        return PullRequestStatsRepoTest::class.java.getResource("/$jsonResponseFile")!!.readText()
    }
    // endregion: Test Utility Functions
}
