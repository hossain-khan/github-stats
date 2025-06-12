package dev.hossain.githubstats

import StatsGeneratorApplication
import dev.hossain.githubstats.di.appModule
import dev.hossain.githubstats.formatter.StatsFormatter
import dev.hossain.githubstats.logging.Log
import kotlinx.coroutines.runBlocking
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.context.stopKoin
import org.koin.java.KoinJavaComponent.inject

/**
 * Main library facade for GitHub Stats functionality.
 *
 * This class provides a simple API for external users to generate GitHub PR review statistics
 * without needing to understand the internal dependency injection and configuration setup.
 *
 * @see GitHubStatsConfig for configuration options
 */
class GitHubStatsLibrary {
    private var isInitialized = false

    /**
     * Initialize the library with the provided configuration.
     * This must be called before using any other library functions.
     *
     * @param config Configuration for the GitHub stats generation
     */
    fun initialize(config: GitHubStatsConfig) {
        if (isInitialized) {
            Log.w("Library is already initialized. Stopping previous instance.")
            stopKoin()
        }

        // Set up the library configuration
        System.setProperty("github.token", config.githubToken)
        System.setProperty("github.repo.owner", config.repoOwner)
        System.setProperty("github.repo.name", config.repoName)

        config.userIds.forEachIndexed { index, userId ->
            System.setProperty("github.user.${index + 1}", userId)
        }

        if (config.dateAfter != null) {
            System.setProperty("date.limit.after", config.dateAfter)
        }

        if (config.dateBefore != null) {
            System.setProperty("date.limit.before", config.dateBefore)
        }

        config.botUserIds?.forEach { botId ->
            System.setProperty("github.bot.user.ids", config.botUserIds.joinToString(","))
        }

        // Set logging level
        BuildConfig.logLevel = config.logLevel

        // Initialize dependency injection
        startKoin {
            modules(appModule)
        }

        isInitialized = true
    }

    /**
     * Generate PR author statistics.
     *
     * @return List of formatted statistics strings for each configured formatter
     */
    fun generateAuthorStats(): List<String> {
        ensureInitialized()

        val statsGeneratorApplication: StatsGeneratorApplication by inject(StatsGeneratorApplication::class.java)
        val results = mutableListOf<String>()

        runBlocking {
            // Capture output from formatters
            val originalFormatters = getFormatters()
            originalFormatters.forEach { formatter ->
                try {
                    statsGeneratorApplication.generateAuthorStats()
                    results.add("Author stats generated successfully using ${formatter::class.simpleName}")
                } catch (e: Exception) {
                    results.add("Error generating author stats with ${formatter::class.simpleName}: ${e.message}")
                }
            }
        }

        return results
    }

    /**
     * Generate PR reviewer statistics.
     *
     * @return List of formatted statistics strings for each configured formatter
     */
    fun generateReviewerStats(): List<String> {
        ensureInitialized()

        val statsGeneratorApplication: StatsGeneratorApplication by inject(StatsGeneratorApplication::class.java)
        val results = mutableListOf<String>()

        runBlocking {
            try {
                statsGeneratorApplication.generateReviewerStats()
                results.add("Reviewer stats generated successfully")
            } catch (e: Exception) {
                results.add("Error generating reviewer stats: ${e.message}")
            }
        }

        return results
    }

    /**
     * Generate both author and reviewer statistics.
     *
     * @return Combined results from both author and reviewer stats generation
     */
    fun generateAllStats(): List<String> {
        val results = mutableListOf<String>()
        results.addAll(generateAuthorStats())
        results.addAll(generateReviewerStats())
        return results
    }

    /**
     * Get raw author statistics data without formatting.
     * Useful for custom processing or formatting.
     *
     * @param authorUserId The GitHub user ID to generate stats for
     * @return AuthorStats object containing raw statistics data
     */
    suspend fun getAuthorStatsData(authorUserId: String): AuthorStats {
        ensureInitialized()

        val prAuthorStatsService: PrAuthorStatsService by inject(PrAuthorStatsService::class.java)
        return prAuthorStatsService.authorStats(authorUserId)
    }

    /**
     * Get raw reviewer statistics data without formatting.
     * Useful for custom processing or formatting.
     *
     * @param reviewerUserId The GitHub user ID to generate stats for
     * @return ReviewerReviewStats object containing raw statistics data
     */
    suspend fun getReviewerStatsData(reviewerUserId: String): ReviewerReviewStats {
        ensureInitialized()

        val prReviewerStatsService: PrReviewerStatsService by inject(PrReviewerStatsService::class.java)
        return prReviewerStatsService.reviewerStats(reviewerUserId)
    }

    /**
     * Clean up resources and stop the library.
     * Call this when you're done using the library.
     */
    fun shutdown() {
        if (isInitialized) {
            stopKoin()
            isInitialized = false
        }
    }

    private fun ensureInitialized() {
        if (!isInitialized) {
            throw IllegalStateException("Library not initialized. Call initialize() first.")
        }
    }

    private fun getFormatters(): List<StatsFormatter> {
        val formatters: List<StatsFormatter> by inject(List::class.java)
        return formatters
    }
}

/**
 * Configuration class for GitHub Stats Library.
 *
 * @param githubToken GitHub personal access token for API authentication
 * @param repoOwner Repository owner (username or organization)
 * @param repoName Repository name
 * @param userIds List of GitHub user IDs to generate stats for
 * @param dateAfter Optional start date for filtering PRs (ISO 8601 format: YYYY-MM-DD)
 * @param dateBefore Optional end date for filtering PRs (ISO 8601 format: YYYY-MM-DD)
 * @param botUserIds Optional list of bot user IDs to exclude from stats
 * @param logLevel Logging level for the library (default: Log.INFO)
 */
data class GitHubStatsConfig(
    val githubToken: String,
    val repoOwner: String,
    val repoName: String,
    val userIds: List<String>,
    val dateAfter: String? = null,
    val dateBefore: String? = null,
    val botUserIds: List<String>? = null,
    val logLevel: Int = Log.INFO,
)
