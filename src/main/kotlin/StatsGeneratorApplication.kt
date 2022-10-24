import dev.hossain.githubstats.AppConstants.LOCAL_PROPERTIES_FILE
import dev.hossain.githubstats.AuthorReviewStats
import dev.hossain.githubstats.BuildConfig
import dev.hossain.githubstats.PrAuthorStatsService
import dev.hossain.githubstats.PrReviewerStatsService
import dev.hossain.githubstats.formatter.StatsFormatter
import dev.hossain.githubstats.util.AppConfig
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.system.measureTimeMillis
import kotlin.time.Duration.Companion.milliseconds

/**
 * App for generating PR stats for users that created PRs and reviewed PRs.
 * See [generateAuthorStats] and [generateReviewerStats] for details.
 */
class StatsGeneratorApplication : KoinComponent {
    private val prReviewerStatsService: PrReviewerStatsService by inject()
    private val prAuthorStatsService: PrAuthorStatsService by inject()

    // Config loader that provides configs from `local.properties`
    private val appConfig: AppConfig by inject()

    // Get all the available stats formatters - such as ASCII table, CSV writer and so on
    private val formatters: List<StatsFormatter> = getKoin().getAll()

    /**
     * Generates stats for user as PR author
     * for all PRs created by each user defined in `[LOCAL_PROPERTIES_FILE]` config file.
     *
     * NOTE: currently, time-zone per user is not supported yet.
     * See https://github.com/hossain-khan/github-stats/issues/129 for details
     */
    suspend fun generateAuthorStats() {
        // Loads the configs defined in `local.properties`
        val (repoOwner, repoId, dateLimit, userIds) = appConfig.get()

        // For each of the users, generates stats for all the PRs created by the user
        userIds.forEach { authorId ->
            println("■ Building stats for `$authorId` as PR author.\n")
            val authorReportBuildTime = measureTimeMillis {
                val prAuthorStats: List<AuthorReviewStats> = prAuthorStatsService.authorStats(
                    owner = repoOwner,
                    repo = repoId,
                    author = authorId,
                    dateLimit = dateLimit
                )

                formatters.forEach {
                    println(it.formatAuthorStats(prAuthorStats))
                }
            }
            if (BuildConfig.DEBUG) {
                println("\nⓘ Stats generation for `$authorId` took ${authorReportBuildTime.milliseconds}")
            }
            println("\n─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─\n")
        }
    }

    /**
     * Generates stats for user as PR reviewer.
     * For all PRs reviewed by each user defined in `[LOCAL_PROPERTIES_FILE]` config file.
     *
     * NOTE: currently, time-zone per user is not supported yet.
     * See https://github.com/hossain-khan/github-stats/issues/129 for details
     */
    suspend fun generateReviewerStats() {
        // Loads the configs defined in `local.properties`
        val (repoOwner, repoId, dateLimit, userIds) = appConfig.get()

        // For each user, generates stats for all the PRs reviewed by the user
        userIds.forEach { usedId ->
            val reviewerReportBuildTime = measureTimeMillis {
                println("■ Building stats for `$usedId` as PR reviewer.\n")
                val prReviewerReviewStats = prReviewerStatsService.reviewerStats(
                    owner = repoOwner,
                    repo = repoId,
                    reviewerUserId = usedId,
                    dateLimit = dateLimit
                )
                formatters.forEach {
                    println(it.formatReviewerStats(prReviewerReviewStats))
                }
            }

            if (BuildConfig.DEBUG) {
                println("\nⓘ Stats generation for `$usedId` took ${reviewerReportBuildTime.milliseconds}")
            }
            println("\n─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─\n")
        }
    }
}
