import dev.hossain.githubstats.AppConstants.LOCAL_PROPERTIES_FILE
import dev.hossain.githubstats.AuthorPrStats
import dev.hossain.githubstats.AuthorStats
import dev.hossain.githubstats.PrAuthorStatsService
import dev.hossain.githubstats.PrReviewerStatsService
import dev.hossain.githubstats.cache.CacheStatsFormatter
import dev.hossain.githubstats.cache.CacheStatsService
import dev.hossain.githubstats.formatter.StatsFormatter
import dev.hossain.githubstats.logging.Log
import dev.hossain.githubstats.util.AppConfig
import dev.hossain.i18n.Resources
import org.koin.core.component.KoinComponent
import kotlin.system.measureTimeMillis
import kotlin.time.Duration.Companion.milliseconds

/**
 * App for generating PR stats for users that created PRs and reviewed PRs.
 * See [generateAuthorStats] and [generateReviewerStats] for details.
 */
class StatsGeneratorApplication(
    private val prReviewerStatsService: PrReviewerStatsService,
    private val prAuthorStatsService: PrAuthorStatsService,
    /**
     * Localized resources for printing messages.
     */
    private val resources: Resources,
    /**
     * Config loader that provides configs from `[LOCAL_PROPERTIES_FILE]`
     */
    private val appConfig: AppConfig,
    /**
     * Get all the available stats formatters - such as ASCII table, CSV writer and so on.
     * @see StatsFormatter
     */
    private val formatters: List<StatsFormatter>,
    /**
     * Cache statistics service for tracking cache performance.
     */
    private val cacheStatsService: CacheStatsService,
    /**
     * Formatter for cache performance statistics.
     */
    private val cacheStatsFormatter: CacheStatsFormatter,
) : KoinComponent {
    /**
     * Generates stats for user as PR author
     * for all PRs created by each user defined in `[LOCAL_PROPERTIES_FILE]` config file.
     *
     * NOTE: currently, time-zone per user is not supported yet.
     * See https://github.com/hossain-khan/github-stats/issues/129 for details
     */
    suspend fun generateAuthorStats() {
        printCurrentAppConfigs()

        // Reset cache statistics at the beginning of the session
        cacheStatsService.reset()

        val allAuthorStats = mutableListOf<AuthorStats>()
        // For each of the users, generates stats for all the PRs created by the user
        appConfig.get().userIds.forEach { authorId ->
            println(resources.string("status_building_author_pr_stats", authorId))

            val authorReportBuildTime =
                measureTimeMillis {
                    val authorStats: AuthorStats = prAuthorStatsService.authorStats(prAuthorUserId = authorId)
                    allAuthorStats.add(authorStats)
                    formatters.forEach {
                        println(it.formatAuthorStats(authorStats))
                    }
                }

            Log.d(resources.string("stats_process_time_for_user", authorId, authorReportBuildTime.milliseconds))
            Log.i("\n─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─\n")
        }

        // Now that we have all author stats, leverage aggregated stats generator
        val aggregatedPrStats: List<AuthorPrStats> = allAuthorStats.map { it.prStats }
        formatters.forEach {
            println(it.formatAllAuthorStats(aggregatedPrStats))
        }

        // Display cache performance statistics at the end
        displayCacheStatistics()
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

        // Reset cache statistics at the beginning of the session
        cacheStatsService.reset()

        // For each user, generates stats for all the PRs reviewed by the user
        appConfig.get().userIds.forEach { userId ->
            val reviewerReportBuildTime =
                measureTimeMillis {
                    println(resources.string("status_building_reviewer_pr_stats", userId))
                    val prReviewerReviewStats = prReviewerStatsService.reviewerStats(prReviewerUserId = userId)
                    formatters.forEach {
                        it.formatReviewerStats(prReviewerReviewStats)
                    }
                }

            Log.d(resources.string("stats_process_time_for_user", userId, reviewerReportBuildTime.milliseconds))
            Log.i("\n─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─\n")
        }

        // Display cache performance statistics at the end
        displayCacheStatistics()
    }

    /**
     * Prints current app configs for visibility when running stats.
     */
    private fun printCurrentAppConfigs() {
        Log.i(resources.string("app_config_snapshot", LOCAL_PROPERTIES_FILE, appConfig.get()))
    }

    /**
     * Displays cache performance statistics at the end of stats generation.
     */
    private fun displayCacheStatistics() {
        try {
            val cacheStats = cacheStatsService.getStats()
            cacheStatsFormatter.logCacheStats(cacheStats)
        } catch (e: Exception) {
            Log.w("Failed to display cache statistics: ${e.message}")
        }
    }
}
