package dev.hossain.githubstats.cache

import app.cash.sqldelight.driver.jdbc.asJdbcDriver
import dev.hossain.githubstats.cache.database.GitHubStatsDatabase
import dev.hossain.githubstats.logging.Log
import dev.hossain.githubstats.util.LocalProperties
import org.postgresql.ds.PGSimpleDataSource
import java.sql.SQLException
import javax.sql.DataSource

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
            Log.d("Setting up database caching...")
            Log.d("Database caching is enabled, initializing database...")

            val jdbcUrl = localProperties.getDbCacheUrl()!!
            val username = localProperties.getDbCacheUsername()!!
            val password = localProperties.getDbCachePassword()!!

            Log.d("Connecting to database: $jdbcUrl")

            // Create PostgreSQL DataSource and convert to SQLDelight driver
            val dataSource = createDataSource(jdbcUrl, username, password)

            // Test the connection first to ensure it's valid
            dataSource.connection.use { connection ->
                connection.isValid(5) // 5 second timeout
            }

            val driver = dataSource.asJdbcDriver()

            // Apply schema creation - ensure database tables exist
            try {
                Log.d("Creating database schema...")
                GitHubStatsDatabase.Schema.create(driver)
                Log.d("Database schema created successfully")
            } catch (e: Exception) {
                Log.w("Schema creation failed, attempting manual table creation: ${e.message}")
                // Fallback: manually create the table if schema creation fails
                createTablesManually(driver)
            }

            // Create database instance
            val dbInstance = GitHubStatsDatabase(driver)
            database = dbInstance

            Log.d("Database caching initialized successfully")
            dbInstance
        } catch (e: SQLException) {
            Log.w("Failed to connect to database: ${e.message}")
            Log.w("Database caching is configured but connection failed - using HTTP cache only")
            null
        } catch (e: Exception) {
            Log.w("Failed to initialize database cache: ${e.message}")
            null
        }
    }

    /**
     * Creates a PostgreSQL DataSource from the JDBC URL and credentials.
     */
    private fun createDataSource(
        jdbcUrl: String,
        username: String,
        password: String,
    ): DataSource {
        val dataSource = PGSimpleDataSource()

        // Parse JDBC URL to extract components
        // Format: jdbc:postgresql://host:port/database
        val url = jdbcUrl.removePrefix("jdbc:postgresql://")
        val parts = url.split("/")
        val hostPort = parts[0]
        val database = if (parts.size > 1) parts[1] else "postgres"

        val hostPortParts = hostPort.split(":")
        val host = hostPortParts[0]
        val port = if (hostPortParts.size > 1) hostPortParts[1].toInt() else 5432

        dataSource.serverNames = arrayOf(host)
        dataSource.portNumbers = intArrayOf(port)
        dataSource.databaseName = database
        dataSource.user = username
        dataSource.password = password

        return dataSource
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

    /**
     * Manually creates the required database tables if schema creation fails.
     * This is a fallback method for when SQLDelight's automatic schema creation doesn't work.
     */
    private fun createTablesManually(driver: app.cash.sqldelight.db.SqlDriver) {
        val createTableSql =
            """
            CREATE TABLE IF NOT EXISTS response_cache (
              id SERIAL PRIMARY KEY,
              cache_key TEXT NOT NULL UNIQUE,
              response_data JSONB NOT NULL,
              created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
              expires_at TIMESTAMP WITH TIME ZONE,
              request_url TEXT NOT NULL,
              http_status INTEGER NOT NULL DEFAULT 200
            );
            
            CREATE INDEX IF NOT EXISTS idx_response_cache_key ON response_cache(cache_key);
            CREATE INDEX IF NOT EXISTS idx_response_cache_expires ON response_cache(expires_at);
            CREATE INDEX IF NOT EXISTS idx_response_cache_url ON response_cache(request_url);
            """.trimIndent()

        try {
            driver.execute(null, createTableSql, 0)
            Log.d("Database tables created manually")
        } catch (ex: Exception) {
            Log.w("Failed to create database tables manually: ${ex.message}")
            throw ex
        }
    }
}
