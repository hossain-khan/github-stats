package dev.hossain.githubstats.client

import dev.hossain.githubstats.cache.CacheStatsService
import dev.hossain.githubstats.io.Client

/**
 * Factory for creating [GitHubApiClient] instances based on configuration.
 */
object GitHubApiClientFactory {
    /**
     * Creates a [GitHubApiClient] based on the specified [clientType].
     *
     * @param clientType The type of client to create
     * @param cacheStatsService Optional cache statistics service (only used for Retrofit client)
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
                        "GitHub CLI is not installed or not available in PATH. " +
                            "Install it with: brew install gh (macOS) or see https://cli.github.com/",
                    )
                }
                GhCliApiClient()
            }
        }
}
