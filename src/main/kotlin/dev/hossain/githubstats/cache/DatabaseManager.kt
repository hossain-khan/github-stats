package dev.hossain.githubstats.cache

import dev.hossain.githubstats.cache.database.GitHubStatsDatabase
import dev.hossain.githubstats.logging.Log
import dev.hossain.githubstats.util.LocalProperties

/**
 * Database connection manager for the GitHub stats cache database.
 *
 * Handles PostgreSQL connection setup and database initialization for
 * caching GitHub API responses using SQLDelight.
 */
object DatabaseManager {
    private var database: GitHubStatsDatabase? = null

    /**
     * Initializes the database connection and creates the GitHubStatsDatabase instance.
     *
     * @param localProperties Configuration properties containing database connection details
     * @return GitHubStatsDatabase instance or null if database is not configured
     */
    fun initializeDatabase(localProperties: LocalProperties): GitHubStatsDatabase? {
        if (!localProperties.isDatabaseCacheEnabled()) {
            Log.d("Database caching is disabled - missing configuration")
            return null
        }

        return try {
            Log.d("Database caching requested but implementation requires runtime database setup")
            Log.w("Database caching feature is not yet fully implemented - falling back to HTTP caching only")
            null
        } catch (e: Exception) {
            Log.w("Failed to initialize database cache: ${e.message}")
            null
        }
    }

    /**
     * Gets the current database instance if available.
     */
    fun getDatabase(): GitHubStatsDatabase? = database

    /**
     * Closes the database connection and cleans up resources.
     */
    fun closeDatabase() {
        database = null
        Log.d("Database connection closed")
    }

    /**
     * Checks if database caching is available and initialized.
     */
    fun isDatabaseAvailable(): Boolean = database != null
}
