package dev.hossain.githubstats.client

/**
 * Enum representing the available GitHub API client implementations.
 */
enum class ApiClientType {
    /**
     * Uses Retrofit with OkHttp for API requests.
     * - Pros: Built-in caching, interceptors, type-safe, better performance
     * - Cons: More dependencies, larger binary size
     */
    RETROFIT,

    /**
     * Uses GitHub CLI (`gh` command) for API requests.
     * - Pros: Uses official GitHub CLI, handles auth automatically, simpler setup
     * - Cons: Requires `gh` CLI installed, slower (shell execution overhead), no built-in caching
     */
    GH_CLI,
    ;

    companion object {
        /**
         * Parses a string to [ApiClientType].
         * Defaults to [RETROFIT] if the string is invalid.
         */
        fun fromString(value: String?): ApiClientType =
            when (value?.uppercase()) {
                "GH_CLI", "GH", "CLI" -> GH_CLI
                else -> RETROFIT
            }
    }
}
