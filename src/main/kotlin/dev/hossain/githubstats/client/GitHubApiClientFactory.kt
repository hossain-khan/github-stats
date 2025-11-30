package dev.hossain.githubstats.client

import dev.hossain.githubstats.cache.CacheStatsCollector
import dev.hossain.githubstats.cache.CacheStatsService
import dev.hossain.githubstats.cache.DatabaseCacheService
import dev.hossain.githubstats.cache.DatabaseManager
import dev.hossain.githubstats.io.Client
import dev.hossain.githubstats.util.LocalProperties

/**
 * Factory for creating [GitHubApiClient] instances based on configuration.
 */
object GitHubApiClientFactory {
    /**
     * Creates a [GitHubApiClient] based on the specified [clientType].
     *
     * @param clientType The type of client to create
     * @param cacheStatsService Optional cache statistics service
     * @return A configured [GitHubApiClient] instance
     * @throws IllegalStateException if GH_CLI is selected but `gh` command is not available
     */
    fun create(
        clientType: ApiClientType,
        cacheStatsService: CacheStatsService? = null,
    ): GitHubApiClient =
        when (clientType) {
            ApiClientType.RETROFIT -> {
                val client = Client(cacheStatsService)
                RetrofitApiClient(client.githubApiService)
            }

            ApiClientType.GH_CLI -> {
                if (!GhCliApiClient.isGhCliAvailable()) {
                    throw IllegalStateException(
                        "GitHub CLI is not installed or not authenticated.\n" +
                            "Install: brew install gh (macOS) or see https://cli.github.com/\n" +
                            "Authenticate: gh auth login",
                    )
                }

                // Setup database cache if enabled
                val databaseCacheService = setupDatabaseCacheForGhCli()

                // Get timeout configuration
                val localProperties = LocalProperties()
                val timeoutSeconds = localProperties.getGhCliTimeoutSeconds()

                GhCliApiClient(
                    databaseCacheService = databaseCacheService,
                    cacheStatsService = cacheStatsService,
                    commandTimeoutSeconds = timeoutSeconds,
                )
            }
        }

    /**
     * Sets up database cache for GH CLI client if configured in local properties.
     * Returns null if database caching is not enabled or setup fails.
     */
    private fun setupDatabaseCacheForGhCli(): DatabaseCacheService? =
        try {
            val localProperties = LocalProperties()
            if (localProperties.isDatabaseCacheEnabled()) {
                val database = DatabaseManager.initializeDatabase(localProperties)
                if (database != null) {
                    DatabaseCacheService(
                        database = database,
                        expirationHours = localProperties.getDbCacheExpirationHours(),
                    )
                } else {
                    null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            // Log warning but don't fail - continue without caching
            System.err.println("Warning: Failed to setup database caching for GH CLI, continuing without cache: ${e.message}")
            null
        }
}
