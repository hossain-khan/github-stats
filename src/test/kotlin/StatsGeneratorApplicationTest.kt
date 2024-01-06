
import dev.hossain.githubstats.AuthorPrStats
import dev.hossain.githubstats.AuthorStats
import dev.hossain.githubstats.PrAuthorStatsService
import dev.hossain.githubstats.PrReviewerStatsService
import dev.hossain.githubstats.ReviewerReviewStats
import dev.hossain.githubstats.formatter.StatsFormatter
import dev.hossain.githubstats.util.AppConfig
import dev.hossain.i18n.Resources
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.time.Duration

/**
 * Unit test for [StatsGeneratorApplication].
 */
class StatsGeneratorApplicationTest {
    private val prReviewerStatsService: PrReviewerStatsService = mockk(relaxed = true)
    private val prAuthorStatsService: PrAuthorStatsService = mockk(relaxed = true)
    private val resources: Resources = mockk(relaxed = true)
    private val appConfig: AppConfig = mockk(relaxed = true)
    private val formatters: List<StatsFormatter> = listOf(mockk(relaxed = true))

    private lateinit var statsGeneratorApplication: StatsGeneratorApplication

    @BeforeEach
    fun setUp() {
        statsGeneratorApplication =
            StatsGeneratorApplication(
                prReviewerStatsService,
                prAuthorStatsService,
                resources,
                appConfig,
                formatters,
            )
    }

    @Test
    fun `generateAuthorStats should call authorStats and formatAuthorStats for each user`() =
        runBlocking {
            val userIds = listOf("user1", "user2")
            val authorStats =
                AuthorStats(
                    AuthorPrStats(
                        authorUserId = "user1",
                        totalPrsCreated = 0,
                        totalIssueComments = 0,
                        totalPrSubmissionComments = 0,
                        totalCodeReviewComments = 0,
                    ),
                    emptyList(),
                )

            coEvery { appConfig.get().userIds } returns userIds
            coEvery { prAuthorStatsService.authorStats(any()) } returns authorStats

            statsGeneratorApplication.generateAuthorStats()

            userIds.forEach { userId ->
                coVerify { prAuthorStatsService.authorStats(userId) }
                formatters.forEach { formatter ->
                    verify { formatter.formatAuthorStats(authorStats) }
                }
            }
        }

    @Test
    fun `generateReviewerStats should call reviewerStats and formatReviewerStats for each user`() =
        runBlocking {
            val userIds = listOf("user1", "user2")
            val reviewerStats =
                ReviewerReviewStats(
                    repoId = "repo_id",
                    reviewerId = "user1",
                    average = Duration.ZERO,
                    totalReviews = 1,
                    reviewedPrStats = emptyList(),
                    reviewedForPrStats = emptyMap(),
                )

            coEvery { appConfig.get().userIds } returns userIds
            coEvery { prReviewerStatsService.reviewerStats(any()) } returns reviewerStats

            statsGeneratorApplication.generateReviewerStats()

            userIds.forEach { userId ->
                coVerify { prReviewerStatsService.reviewerStats(userId) }
                formatters.forEach { formatter ->
                    verify { formatter.formatReviewerStats(reviewerStats) }
                }
            }
        }
}
