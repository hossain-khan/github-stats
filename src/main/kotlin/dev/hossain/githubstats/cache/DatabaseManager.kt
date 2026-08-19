package dev.hossain.githubstats.cache

import app.cash.sqldelight.driver.jdbc.asJdbcDriver
import dev.hossain.githubstats.cache.database.postgres.PostgreSqlDatabase
import dev.hossain.githubstats.cache.database.sqlite.SqliteDatabase
import dev.hossain.githubstats.logging.Log
import dev.hossain.githubstats.util.LocalProperties
import org.postgresql.ds.PGSimpleDataSource
import org.sqlite.SQLiteDataSource
import java.sql.SQLException
import javax.sql.DataSource

/**
 * Database connection manager for GitHub stats response cache databases.
 *
 * Handles database setup and initialization for both SQLite and PostgreSQL using SQLDelight.
 */
object DatabaseManager {
    private var cacheService: DatabaseCacheService? = null

    /**
     * Initializes and returns the [DatabaseCacheService] based on configuration in [LocalProperties].
     *
     * @param localProperties Configuration properties containing database connection details
     * @return [DatabaseCacheService] instance or null if database caching is disabled or fails to initialize
     */
    fun createDatabaseCacheService(localProperties: LocalProperties): DatabaseCacheService? {
        if (!localProperties.isDatabaseCacheEnabled()) {
            Log.d("Database caching is disabled")
            return null
        }

        return when (localProperties.getDbCacheType()) {
            DatabaseType.SQLITE -> initializeSqlite(localProperties)
            DatabaseType.POSTGRESQL -> initializePostgreSql(localProperties)
            DatabaseType.NONE -> null
        }
    }

    /**
     * Initializes a local file-based SQLite database for response caching.
     */
    private fun initializeSqlite(localProperties: LocalProperties): DatabaseCacheService? =
        try {
            val sqliteFile = localProperties.getDbCacheSqliteFile()
            Log.d("Setting up SQLite database caching: $sqliteFile")

            val dataSource =
                SQLiteDataSource().apply {
                    url = "jdbc:sqlite:$sqliteFile"
                }

            val driver = dataSource.asJdbcDriver()

            try {
                Log.d("Creating SQLite database schema if not exists...")
                SqliteDatabase.Schema.create(driver)
            } catch (e: Exception) {
                Log.d("SQLite database schema initialized: ${e.message}")
            }

            val dbInstance = SqliteDatabase(driver)
            val service = SqliteDatabaseCacheService(dbInstance, localProperties.getDbCacheExpirationHours())
            cacheService = service

            Log.d("SQLite database caching initialized successfully")
            service
        } catch (e: Exception) {
            Log.w("Failed to initialize SQLite database cache: ${e.message}")
            null
        }

    /**
     * Initializes a PostgreSQL database connection for response caching.
     */
    private fun initializePostgreSql(localProperties: LocalProperties): DatabaseCacheService? =
        try {
            val jdbcUrl = localProperties.getDbCacheUrl()!!
            val username = localProperties.getDbCacheUsername()!!
            val password = localProperties.getDbCachePassword()!!

            Log.d("Connecting to PostgreSQL database: $jdbcUrl")
            val dataSource = createPostgreSqlDataSource(jdbcUrl, username, password)

            dataSource.connection.use { connection ->
                connection.isValid(5) // 5 second timeout
            }

            val driver = dataSource.asJdbcDriver()

            try {
                Log.d("Creating PostgreSQL database schema if not exists...")
                PostgreSqlDatabase.Schema.create(driver)
            } catch (e: Exception) {
                Log.w("PostgreSQL schema initialization notice: ${e.message}")
            }

            val dbInstance = PostgreSqlDatabase(driver)
            val service = PostgreSqlDatabaseCacheService(dbInstance, localProperties.getDbCacheExpirationHours())
            cacheService = service

            Log.d("PostgreSQL database caching initialized successfully")
            service
        } catch (e: SQLException) {
            Log.w("Failed to connect to PostgreSQL database: ${e.message}")
            Log.w("Database caching is configured but connection failed - using HTTP cache only")
            null
        } catch (e: Exception) {
            Log.w("Failed to initialize PostgreSQL database cache: ${e.message}")
            null
        }

    /**
     * Creates a PostgreSQL DataSource from the JDBC URL and credentials.
     */
    private fun createPostgreSqlDataSource(
        jdbcUrl: String,
        username: String,
        password: String,
    ): DataSource {
        val dataSource = PGSimpleDataSource()

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
     * Gets the active database cache service if initialized.
     */
    fun getCacheService(): DatabaseCacheService? = cacheService

    /**
     * Closes the active database connection and cleans up resources.
     */
    fun closeDatabase() {
        cacheService = null
        Log.d("Database cache service closed")
    }

    /**
     * Checks if database caching is available and initialized.
     */
    fun isDatabaseAvailable(): Boolean = cacheService != null
}
