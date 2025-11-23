package dev.hossain.githubstats.util

import dev.hossain.githubstats.AppConstants.LOCAL_PROPERTIES_FILE
import dev.hossain.githubstats.AppConstants.LOCAL_PROPERTIES_SAMPLE_FILE
import java.io.File
import java.util.Properties

/**
 * Internal class to get properties
 */
abstract class PropertiesReader(
    fileName: String,
) {
    private val properties = Properties()

    init {
        val propertiesFile = File(fileName)
        if (propertiesFile.exists()) {
            properties.load(propertiesFile.inputStream())
        } else {
            if (System.getenv("IS_GITHUB_CI") == "true") {
                properties.load(File(LOCAL_PROPERTIES_SAMPLE_FILE).inputStream())
            } else {
                throw IllegalStateException(
                    "Please create `$LOCAL_PROPERTIES_FILE` with config values. See `$LOCAL_PROPERTIES_SAMPLE_FILE`.",
                )
            }
        }
    }

    fun getProperty(key: String): String? = properties.getProperty(key)
}

class LocalProperties : PropertiesReader(LOCAL_PROPERTIES_FILE) {
    companion object {
        private const val KEY_API_CLIENT_TYPE = "api_client_type"
        private const val KEY_REPO_OWNER = "repository_owner"
        private const val KEY_REPO_ID = "repository_id"
        private const val KEY_AUTHOR_IDS = "authors"
        private const val KEY_BOT_USERS = "bot_users"
        private const val KEY_DATE_LIMIT_AFTER = "date_limit_after"
        private const val KEY_DATE_LIMIT_BEFORE = "date_limit_before"

        // Database cache configuration keys
        private const val KEY_DB_CACHE_URL = "db_cache_url"
        private const val KEY_DB_CACHE_USERNAME = "db_cache_username"
        private const val KEY_DB_CACHE_PASSWORD = "db_cache_password"
        private const val KEY_DB_CACHE_EXPIRATION_HOURS = "db_cache_expiration_hours"

        // GH CLI configuration keys
        private const val KEY_GH_CLI_TIMEOUT_SECONDS = "gh_cli_timeout_seconds"
    }

    /**
     * Gets the API client type configuration.
     * Defaults to RETROFIT if not specified.
     */
    fun getApiClientType(): String = getProperty(KEY_API_CLIENT_TYPE) ?: "RETROFIT"

    fun getRepoOwner(): String =
        requireNotNull(getProperty(KEY_REPO_OWNER)) {
            "Repository owner also known as Org ID config is required in $LOCAL_PROPERTIES_FILE"
        }

    fun getRepoId(): String =
        requireNotNull(getProperty(KEY_REPO_ID)) {
            "Repository ID config is required in $LOCAL_PROPERTIES_FILE"
        }

    fun getAuthors(): String? = getProperty(KEY_AUTHOR_IDS)

    fun getBotUsers(): String? = getProperty(KEY_BOT_USERS)

    fun getDateLimitAfter(): String? = getProperty(KEY_DATE_LIMIT_AFTER)

    fun getDateLimitBefore(): String? = getProperty(KEY_DATE_LIMIT_BEFORE)

    /**
     * Gets database cache URL for PostgreSQL connection.
     * For example: `jdbc:postgresql://some.example.com:5432/github_stats_cache`
     * Returns null if database caching is not configured.
     * @throws IllegalArgumentException if the URL format is invalid
     */
    fun getDbCacheUrl(): String? {
        val url = getProperty(KEY_DB_CACHE_URL)
        if (url != null) {
            validatePostgreSqlUrl(url)
        }
        return url
    }

    /**
     * Gets database cache username for PostgreSQL connection.
     * Returns null if database caching is not configured.
     */
    fun getDbCacheUsername(): String? = getProperty(KEY_DB_CACHE_USERNAME)

    /**
     * Gets database cache password for PostgreSQL connection.
     * Returns null if database caching is not configured.
     */
    fun getDbCachePassword(): String? = getProperty(KEY_DB_CACHE_PASSWORD)

    /**
     * Gets database cache expiration time in hours.
     * Defaults to 24 hours if not specified.
     */
    fun getDbCacheExpirationHours(): Long = getProperty(KEY_DB_CACHE_EXPIRATION_HOURS)?.toLongOrNull() ?: 24L

    /**
     * Checks if database caching is configured by verifying required properties are present.
     */
    fun isDatabaseCacheEnabled(): Boolean =
        !getDbCacheUrl().isNullOrBlank() &&
            !getDbCacheUsername().isNullOrBlank() &&
            !getDbCachePassword().isNullOrBlank()

    /**
     * Gets GH CLI command timeout in seconds.
     * Defaults to 30 seconds if not specified.
     * Used to prevent indefinite hangs when executing gh CLI commands.
     */
    fun getGhCliTimeoutSeconds(): Long = getProperty(KEY_GH_CLI_TIMEOUT_SECONDS)?.toLongOrNull() ?: 30L

    /**
     * Validates that the database URL follows the PostgreSQL JDBC format.
     * Expected format: jdbc:postgresql://host[:port]/database
     * Port is optional and defaults to 5432 if not specified.
     */
    private fun validatePostgreSqlUrl(url: String) {
        val postgresUrlPattern =
            Regex(
                "^jdbc:postgresql://[a-zA-Z0-9.-]+(?::[0-9]+)?/[a-zA-Z0-9_]+$",
            )

        if (!postgresUrlPattern.matches(url)) {
            throw IllegalArgumentException(
                "Invalid PostgreSQL JDBC URL format: '$url'. " +
                    "Expected format: jdbc:postgresql://host[:port]/database " +
                    "(e.g., jdbc:postgresql://localhost:5432/github_stats_cache or jdbc:postgresql://host.com/database)",
            )
        }
    }
}
