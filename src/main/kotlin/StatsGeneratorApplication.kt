import dev.hossain.githubstats.AppConstants.LOCAL_PROPERTIES_FILE
import dev.hossain.githubstats.AuthorPrStats
import dev.hossain.githubstats.AuthorStats
import dev.hossain.githubstats.PrAuthorStatsService
import dev.hossain.githubstats.PrReviewerStatsService
import dev.hossain.githubstats.formatter.StatsFormatter
import dev.hossain.githubstats.logging.Log
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

    /**
     * Config loader that provides configs from `[LOCAL_PROPERTIES_FILE]`
     */
    private val appConfig: AppConfig by inject()

    /**
     * Get all the available stats formatters - such as ASCII table, CSV writer and so on.
     * @see StatsFormatter
     */
    private val formatters: List<StatsFormatter> = getKoin().getAll()

    /**
     * Generates stats for user as PR author
     * for all PRs created by each user defined in `[LOCAL_PROPERTIES_FILE]` config file.
     *
     * NOTE: currently, time-zone per user is not supported yet.
     * See https://github.com/hossain-khan/github-stats/issues/129 for details
     */
    suspend fun generateAuthorStats() {
        printCurrentAppConfigs()

        val allAuthorStats = mutableListOf<AuthorStats>()
        // For each of the users, generates stats for all the PRs created by the user
        appConfig.get().userIds.forEach { authorId ->
            println("■ Building stats for `$authorId` as PR author.\n")

            val authorReportBuildTime = measureTimeMillis {
                val authorStats: AuthorStats = prAuthorStatsService.authorStats(prAuthorUserId = authorId)
                allAuthorStats.add(authorStats)
                formatters.forEach {
                    println(it.formatAuthorStats(authorStats))
                }
            }

            Log.d("\nⓘ Stats generation for `$authorId` took ${authorReportBuildTime.milliseconds}")
            Log.i("\n─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─\n")
        }

        // Now that we have all author stats, leverage aggregated stats generator
        val aggregatedPrStats: List<AuthorPrStats> = allAuthorStats.map { it.prStats }
        formatters.forEach {
            println(it.formatAllAuthorStats(aggregatedPrStats))
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
        printCurrentAppConfigs()
        // For each user, generates stats for all the PRs reviewed by the user
        appConfig.get().userIds.forEach { usedId ->
            val reviewerReportBuildTime = measureTimeMillis {
                println("■ Building stats for `$usedId` as PR reviewer.\n")
                val prReviewerReviewStats = prReviewerStatsService.reviewerStats(prReviewerUserId = usedId)
                formatters.forEach {
                    println(it.formatReviewerStats(prReviewerReviewStats))
                }
            }

            Log.d("\nⓘ Stats generation for `$usedId` took ${reviewerReportBuildTime.milliseconds}")
            Log.i("\n─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─\n")
        }
    }

    /**
     * Prints current app configs for visibility when running stats.
     */
    private fun printCurrentAppConfigs() {
        Log.i(
            "\nⓘ Loaded current app configs from $LOCAL_PROPERTIES_FILE: " +
                "\n${appConfig.get()}\n"
        )
    }
}
