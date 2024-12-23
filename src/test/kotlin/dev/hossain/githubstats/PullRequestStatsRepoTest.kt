package dev.hossain.githubstats

import com.google.common.truth.Truth.assertThat
import dev.hossain.githubstats.io.Client
import dev.hossain.githubstats.repository.PullRequestStatsRepo.StatsResult
import dev.hossain.githubstats.repository.PullRequestStatsRepoImpl
import dev.hossain.githubstats.service.TimelineEventsPagerService
import dev.hossain.githubstats.util.ErrorProcessor
import dev.hossain.time.UserTimeZone
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
@Suppress("ktlint:standard:max-line-length")
internal class PullRequestStatsRepoTest {
    // https://github.com/square/okhttp/tree/master/mockwebserver
    private lateinit var mockWebServer: MockWebServer
    private lateinit var pullRequestStatsRepo: PullRequestStatsRepoImpl

    private companion object {
        const val REPO_OWNER = "owner"
        const val REPO_ID = "repository-name"
    }

    @BeforeEach
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start(60000)
        Client.baseUrl = mockWebServer.url("/")

        pullRequestStatsRepo =
            PullRequestStatsRepoImpl(
                githubApiService = Client.githubApiService,
                timelinesPager =
                    TimelineEventsPagerService(
                        githubApiService = Client.githubApiService,
                        errorProcessor = ErrorProcessor(),
                    ),
                userTimeZone = UserTimeZone(),
            )
    }

    @AfterEach
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `stats - given pull request not merged - provides failure status`() =
        runTest {
            // Uses data from https://github.com/jquery/jquery/pull/5046
            mockWebServer.enqueue(MockResponse().setBody(respond("pulls-freeCodeCamp-48543-not-merged.json")))

            val calculateStats = pullRequestStatsRepo.stats(REPO_OWNER, REPO_ID, 123, emptyList())

            assertThat(calculateStats).isInstanceOf(StatsResult.Failure::class.java)
        }

    @Test
    fun `stats - given pr review - calculates time taken to provide review`() =
        runTest {
            // Uses data from https://github.com/jquery/jquery/pull/5046
            mockWebServer.enqueue(MockResponse().setBody(respond("pulls-jquery-5046.json")))
            mockWebServer.enqueue(MockResponse().setBody(respond("timeline-jquery-5046.json")))
            mockWebServer.enqueue(MockResponse().setBody("[]")) // PR Review comments

            val calculateStats = pullRequestStatsRepo.stats(REPO_OWNER, REPO_ID, 123, emptyList())

            assertThat(calculateStats).isInstanceOf(StatsResult.Success::class.java)
        }

    @Test
    fun `stats - given many pr comments and review - calculates only the approval time`() =
        runTest {
            // Uses data from https://github.com/opensearch-project/OpenSearch/pull/4515
            mockWebServer.enqueue(MockResponse().setBody(respond("pulls-opensearch-4515.json")))
            mockWebServer.enqueue(MockResponse().setBody(respond("timeline-opensearch-4515.json")))
            mockWebServer.enqueue(MockResponse().setBody("[]")) // PR Review comments

            val statsResult = pullRequestStatsRepo.stats(REPO_OWNER, REPO_ID, 123, emptyList())

            assertThat(statsResult).isInstanceOf(StatsResult.Success::class.java)
        }

    @Test
    fun `stats - given reviewer was added later - provides time after reviewer was added`() =
        runTest {
            // Uses data from https://github.com/freeCodeCamp/freeCodeCamp/pull/47594
            // User `naomi-lgbt` was added later in the PR.
            // This is also interesting PR because changes was requested (See issue #51)

            mockWebServer.enqueue(MockResponse().setBody(respond("pulls-freeCodeCamp-47594.json")))
            mockWebServer.enqueue(MockResponse().setBody(respond("timeline-freeCodeCamp-47594.json")))
            mockWebServer.enqueue(MockResponse().setBody("[]")) // PR Review comments

            val statsResult = pullRequestStatsRepo.stats(REPO_OWNER, REPO_ID, 123, emptyList())

            assertThat(statsResult).isInstanceOf(StatsResult.Success::class.java)

            val result = statsResult as StatsResult.Success
            val reviewTime = result.stats.prApprovalTime["naomi-lgbt"]
            assertThat(reviewTime).isLessThan(Duration.parse("13h"))
        }

    @Test
    fun `stats - given multiple reviews and dismissed reviews - provides stats accordingly`() =
        runTest {
            // Uses data from https://github.com/freeCodeCamp/freeCodeCamp/pull/47550
            // A lot of review comments, 2 people approved after dismissal
            mockWebServer.enqueue(MockResponse().setBody(respond("pulls-freeCodeCamp-47550.json")))
            mockWebServer.enqueue(MockResponse().setBody(respond("timeline-freeCodeCamp-47550.json")))
            mockWebServer.enqueue(MockResponse().setBody("[]")) // PR Review comments

            val statsResult = pullRequestStatsRepo.stats(REPO_OWNER, REPO_ID, 123, emptyList())

            assertThat(statsResult).isInstanceOf(StatsResult.Success::class.java)

            val result = statsResult as StatsResult.Success
            val reviewTime = result.stats.prApprovalTime
            assertThat(reviewTime).hasSize(2)
        }

    @Test
    fun `stats - pr creator commented on PR - does not contain review metrics for pr creator`() =
        runTest {
            // Uses data from https://github.com/square/retrofit/pull/3267
            mockWebServer.enqueue(MockResponse().setBody(respond("pulls-retrofit-3267.json")))
            mockWebServer.enqueue(MockResponse().setBody(respond("timeline-retrofit-3267.json")))
            mockWebServer.enqueue(MockResponse().setBody("[]")) // PR Review comments

            val statsResult = pullRequestStatsRepo.stats(REPO_OWNER, REPO_ID, 123, emptyList())

            assertThat(statsResult).isInstanceOf(StatsResult.Success::class.java)

            val result = statsResult as StatsResult.Success
            assertThat(result.stats.prApprovalTime)
                .doesNotContainKey("JakeWharton")
        }

    @Test
    fun `stats - given merged with no reviewer - provides no related metrics`() =
        runTest {
            // Uses data from https://github.com/hossain-khan/github-stats/pull/27
            mockWebServer.enqueue(MockResponse().setBody(respond("pulls-githubstats-27.json")))
            mockWebServer.enqueue(MockResponse().setBody(respond("timeline-githubstats-27.json")))
            mockWebServer.enqueue(MockResponse().setBody("[]")) // PR Review comments

            val calculateStats = pullRequestStatsRepo.stats(REPO_OWNER, REPO_ID, 123, emptyList())

            assertThat(calculateStats).isInstanceOf(StatsResult.Failure::class.java)
        }

    @Test
    fun `stats - given merged with bot user as reviewer - provides failure status`() =
        runTest {
            // Uses data from https://github.com/hossain-khan/github-stats/pull/27
            mockWebServer.enqueue(MockResponse().setBody(respond("pulls-githubstats-27.json")))
            mockWebServer.enqueue(MockResponse().setBody(respond("timeline-githubstats-27-bot-user.json")))
            mockWebServer.enqueue(MockResponse().setBody("[]")) // PR Review comments

            val calculateStats = pullRequestStatsRepo.stats(REPO_OWNER, REPO_ID, 123, listOf("BotUser"))

            assertThat(calculateStats).isInstanceOf(StatsResult.Failure::class.java)
        }

    @Test
    fun `stats - given pr created by ignored bot user - provides failure status`() =
        runTest {
            // Uses data from https://github.com/jquery/jquery/pull/5046
            mockWebServer.enqueue(MockResponse().setBody(respond("pulls-jquery-5046-bot-user.json")))
            mockWebServer.enqueue(MockResponse().setBody(respond("timeline-jquery-5046.json")))
            mockWebServer.enqueue(MockResponse().setBody("[]")) // PR Review comments

            val calculateStats = pullRequestStatsRepo.stats(REPO_OWNER, REPO_ID, 123, listOf("BotUser"))

            assertThat(calculateStats).isInstanceOf(StatsResult.Failure::class.java)
        }

    @Test
    fun `stats - given pr was draft - provides time taken after pr was ready for review`() =
        runTest {
        }

    @Test
    fun `stats - given no assigned reviewer added - provides metrics based on approval event`() =
        runTest {
            // Uses data from https://github.com/square/retrofit/pull/3114
            mockWebServer.enqueue(MockResponse().setBody(respond("pulls-retrofit-3114.json")))
            mockWebServer.enqueue(MockResponse().setBody(respond("timeline-retrofit-3114.json")))
            mockWebServer.enqueue(MockResponse().setBody("[]")) // PR Review comments

            val calculateStats = pullRequestStatsRepo.stats(REPO_OWNER, REPO_ID, 123, emptyList())

            assertThat(calculateStats).isInstanceOf(StatsResult.Failure::class.java)
        }

    @Test
    fun `stats - given pr reviewed in time - provides correct review time`() =
        runTest {
            mockWebServer.enqueue(MockResponse().setBody(respond("pulls-freeCodeCamp-47511.json")))
            mockWebServer.enqueue(MockResponse().setBody(respond("timeline-freeCodeCamp-45711.json")))
            mockWebServer.enqueue(MockResponse().setBody("[]")) // PR Review comments

            val statsResult = pullRequestStatsRepo.stats(REPO_OWNER, REPO_ID, 123, emptyList())
            val result = statsResult as StatsResult.Success
            assertThat(result.stats.prApprovalTime).hasSize(2)
            assertThat(result.stats.prApprovalTime["DanielRosa74"]).isEqualTo(Duration.parse("7m"))
        }

    @Test
    fun `stats - given pr has multiple issue comments - provides comment count for each user`() =
        runTest {
            // Lots of comments by different users
            // Uses data from https://github.com/square/okhttp/pull/3873
            mockWebServer.enqueue(MockResponse().setBody(respond("pulls-okhttp-3873.json")))
            mockWebServer.enqueue(MockResponse().setBody(respond("timeline-okhttp-3873.json")))
            mockWebServer.enqueue(MockResponse().setBody("[]")) // PR Review comments

            val statsResult = pullRequestStatsRepo.stats(REPO_OWNER, REPO_ID, 123, emptyList())
            val stats = (statsResult as StatsResult.Success).stats
            assertThat(stats.comments).hasSize(5)
            assertThat(stats.comments.keys)
                .containsExactlyElementsIn(setOf("swankjesse", "jjshanks", "yschimke", "mjpitz", "JakeWharton"))
            assertThat(stats.comments["swankjesse"]!!.issueComment).isEqualTo(3)
        }

    @Test
    fun `stats - given pr has multiple issue and review comments - provides all comment count for each user`() =
        runTest {
            // Lots of comments by different users
            // Uses data from https://github.com/square/okhttp/pull/3873
            mockWebServer.enqueue(MockResponse().setBody(respond("pulls-okhttp-3873.json")))
            mockWebServer.enqueue(MockResponse().setBody(respond("timeline-okhttp-3873.json")))
            mockWebServer.enqueue(MockResponse().setBody(respond("pulls-num-comments-okhttp-3873.json")))

            val statsResult = pullRequestStatsRepo.stats(REPO_OWNER, REPO_ID, 123, emptyList())
            val stats = (statsResult as StatsResult.Success).stats
            assertThat(stats.comments).hasSize(5)
            assertThat(stats.comments.keys)
                .containsExactlyElementsIn(setOf("swankjesse", "jjshanks", "yschimke", "mjpitz", "JakeWharton"))

            val userPrComment: UserPrComment = stats.comments["yschimke"]!!

            // yschimke made 9 PR comment, 14 pr review and 21 code review comment. Total: 44 comments.
            assertThat(userPrComment.issueComment).isEqualTo(9)
            assertThat(userPrComment.codeReviewComment).isEqualTo(21)
            assertThat(userPrComment.prReviewSubmissionComment).isEqualTo(14)
            assertThat(userPrComment.allComments).isEqualTo(44)
        }

    @Test
    fun `stats - given multiple reviewers - provides stats for initial response time accordingly`() =
        runTest {
            // Uses data from https://github.com/freeCodeCamp/freeCodeCamp/pull/47550
            // A lot of review comments by 5 people, and 2 people approved after dismissal
            // PR was opened on weekend @ Saturday, Sep 17, 2022 at 6pm
            // Most approval was        @ Thursday, Sept 22, 2022 at 5:30pm
            mockWebServer.enqueue(MockResponse().setBody(respond("pulls-freeCodeCamp-47550.json")))
            mockWebServer.enqueue(MockResponse().setBody(respond("timeline-freeCodeCamp-47550.json")))
            mockWebServer.enqueue(MockResponse().setBody("[]")) // PR Review comments

            val statsResult = pullRequestStatsRepo.stats(REPO_OWNER, REPO_ID, 123, emptyList())

            assertThat(statsResult).isInstanceOf(StatsResult.Success::class.java)

            val result = statsResult as StatsResult.Success
            val initialResponseTime = result.stats.initialResponseTime
            assertThat(initialResponseTime).hasSize(5)

            assertThat(initialResponseTime["ShaunSHamilton"]).isEqualTo(Duration.parse("0s")) // Before work-hour
            assertThat(initialResponseTime["SaintPeter"]).isEqualTo(Duration.parse("2h 24m"))
            assertThat(initialResponseTime["jeremylt"]).isEqualTo(Duration.parse("2h 49m"))
            assertThat(initialResponseTime["naomi-lgbt"]).isEqualTo(Duration.parse("8h"))
            assertThat(initialResponseTime["Sboonny"]).isEqualTo(Duration.parse("1d"))

            val prApprovalTime = result.stats.prApprovalTime
            assertThat(prApprovalTime).hasSize(2)
            assertThat(prApprovalTime["naomi-lgbt"]).isEqualTo(Duration.parse("1d 8h"))
            assertThat(prApprovalTime["ShaunSHamilton"]).isEqualTo(Duration.parse("1d 8h"))
        }


    @Test
    fun `stats - given PR opened on weekend and approved by 2 reviewers - provides correct review time`() =
        runTest {
            // PR opened on weekend and approved by 2 reviewers
            // Uses data from https://github.com/freeCodeCamp/freeCodeCamp/pull/56555
            mockWebServer.enqueue(MockResponse().setBody(respond("pulls-freeCodeCamp-56555.json")))
            mockWebServer.enqueue(MockResponse().setBody(respond("timeline-freeCodeCamp-56555.json")))
            mockWebServer.enqueue(MockResponse().setBody("[]")) // PR Review comments

            val statsResult = pullRequestStatsRepo.stats(REPO_OWNER, REPO_ID, 123, emptyList())

            assertThat(statsResult).isInstanceOf(StatsResult.Success::class.java)

            val result = statsResult as StatsResult.Success
            val initialResponseTime = result.stats.initialResponseTime
            val prApprovalTime = result.stats.prApprovalTime
            assertThat(initialResponseTime).hasSize(2)

            assertThat(initialResponseTime["naomi-lgbt"]).isEqualTo(Duration.parse("8h"))
            assertThat(initialResponseTime["gikf"]).isEqualTo(Duration.parse("20h 11m"))

            assertThat(prApprovalTime["gikf"]).isEqualTo(Duration.parse("20h 11m"))
            assertThat(prApprovalTime["naomi-lgbt"]).isEqualTo(Duration.parse("19h 53m"))
        }

    @Test
    fun `stats - given pr was reviewed earlier and then later approved by same reviewer - provides stats for initial response and review time accordingly`() =
        runTest {
            // Uses data from https://github.com/freeCodeCamp/freeCodeCamp/pull/47550
            // The reviewer `RandellDawson` has requested change first and later approved
            mockWebServer.enqueue(MockResponse().setBody(respond("pulls-freeCodeCamp-48266.json")))
            mockWebServer.enqueue(MockResponse().setBody(respond("timeline-freeCodeCamp-48266.json")))
            mockWebServer.enqueue(MockResponse().setBody("[]")) // PR Review comments

            val statsResult = pullRequestStatsRepo.stats(REPO_OWNER, REPO_ID, 123, emptyList())

            assertThat(statsResult).isInstanceOf(StatsResult.Success::class.java)

            val result = statsResult as StatsResult.Success
            val initialResponseTime = result.stats.initialResponseTime
            val prApprovalTime = result.stats.prApprovalTime
            assertThat(initialResponseTime).hasSize(3)
            assertThat(prApprovalTime).hasSize(3)

            assertThat(initialResponseTime["RandellDawson"]).isEqualTo(Duration.parse("12h"))
            assertThat(prApprovalTime["RandellDawson"]).isEqualTo(Duration.parse("1d 10h 23m"))
        }

    @Test
    fun `prReviewers - given pr has multiple reviewers - calculates all reviewer user-ids`() =
        runTest {
            // Uses data from https://github.com/opensearch-project/OpenSearch/pull/4515
            mockWebServer.enqueue(MockResponse().setBody(respond("pulls-freeCodeCamp-47550.json")))
            mockWebServer.enqueue(MockResponse().setBody(respond("timeline-freeCodeCamp-47550.json")))

            val pullRequest = Client.githubApiService.pullRequest("X", "Y", 1)
            val timelineEvents = Client.githubApiService.timelineEvents("X", "Y", 1)

            val prReviewers = pullRequestStatsRepo.prReviewers(pullRequest.user, timelineEvents)

            assertThat(prReviewers).hasSize(5)
            assertThat(prReviewers).doesNotContain(pullRequest.user)
        }

    @Test
    fun `prReviewers - given PR did not have assigned reviewers but reviewers self reviewed - provides those reviewers user-ids`() =
        runTest {
            // Uses data from https://github.com/square/okhttp/pull/3873
            mockWebServer.enqueue(MockResponse().setBody(respond("pulls-okhttp-3873.json")))
            mockWebServer.enqueue(MockResponse().setBody(respond("timeline-okhttp-3873.json")))

            val pullRequest = Client.githubApiService.pullRequest("X", "Y", 1)
            val timelineEvents = Client.githubApiService.timelineEvents("X", "Y", 1)

            val prReviewers = pullRequestStatsRepo.prReviewers(pullRequest.user, timelineEvents)

            assertThat(prReviewers).doesNotContain(pullRequest.user)
            assertThat(prReviewers).hasSize(2)
            assertThat(prReviewers.map { it.login }).containsExactly("yschimke", "swankjesse")
        }

    @Test
    fun `prReviewers - given single reviewer approved PR - provides reviewers user-id`() =
        runTest {
            // Uses data from https://github.com/square/okhttp/pull/7458
            mockWebServer.enqueue(MockResponse().setBody(respond("pulls-okhttp-7458.json")))
            mockWebServer.enqueue(MockResponse().setBody(respond("timeline-okhttp-7458.json")))

            val pullRequest = Client.githubApiService.pullRequest("X", "Y", 1)
            val timelineEvents = Client.githubApiService.timelineEvents("X", "Y", 1)

            val prReviewers = pullRequestStatsRepo.prReviewers(pullRequest.user, timelineEvents)

            assertThat(prReviewers).doesNotContain(pullRequest.user)
            assertThat(prReviewers).hasSize(1)
            assertThat(prReviewers.map { it.login }).containsExactly("swankjesse")
        }

    // region: Test Utility Functions

    /** Provides response for given [jsonResponseFile] path in the test resources. */
    private fun respond(jsonResponseFile: String): String =
        PullRequestStatsRepoTest::class.java.getResource("/$jsonResponseFile")!!.readText()
    // endregion: Test Utility Functions
}
