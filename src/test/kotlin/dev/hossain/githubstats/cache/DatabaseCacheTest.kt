package dev.hossain.githubstats.cache

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.google.common.truth.Truth.assertThat
import dev.hossain.githubstats.cache.database.sqlite.SqliteDatabase
import dev.hossain.githubstats.util.LocalProperties
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.io.File

/**
 * Tests for database cache related functionality for SQLite and PostgreSQL.
 */
class DatabaseCacheTest {
    @Test
    fun `database manager returns null when caching is disabled`() {
        // Given
        val localProperties = mockk<LocalProperties>()
        every { localProperties.isDatabaseCacheEnabled() } returns false

        // When
        val result = DatabaseManager.createDatabaseCacheService(localProperties)

        // Then
        assertThat(result).isNull()
    }

    @Test
    fun `database manager logs configuration found but returns null when postgres connection fails`() {
        // Given
        val localProperties = mockk<LocalProperties>()
        every { localProperties.isDatabaseCacheEnabled() } returns true
        every { localProperties.getDbCacheType() } returns DatabaseType.POSTGRESQL
        every { localProperties.getDbCacheUrl() } returns "jdbc:postgresql://invalid-host:9999/nonexistent_db"
        every { localProperties.getDbCacheUsername() } returns "invalid_user"
        every { localProperties.getDbCachePassword() } returns "invalid_pass"
        every { localProperties.getDbCacheExpirationHours() } returns 24L

        // When
        val result = DatabaseManager.createDatabaseCacheService(localProperties)

        // Then
        assertThat(result).isNull() // Connection will fail gracefully
    }

    @Test
    fun `local properties database config validation works correctly for postgres and sqlite`() {
        // Given - Postgres enabled
        val postgresEnabled = mockk<LocalProperties>()
        every { postgresEnabled.getDbCacheType() } returns DatabaseType.POSTGRESQL
        every { postgresEnabled.getDbCacheUrl() } returns "jdbc:postgresql://localhost:5432/test"
        every { postgresEnabled.getDbCacheUsername() } returns "user"
        every { postgresEnabled.getDbCachePassword() } returns "pass"
        every { postgresEnabled.isDatabaseCacheEnabled() } answers { callOriginal() }

        // Given - SQLite enabled
        val sqliteEnabled = mockk<LocalProperties>()
        every { sqliteEnabled.getDbCacheType() } returns DatabaseType.SQLITE
        every { sqliteEnabled.getDbCacheSqliteFile() } returns "test-cache.db"
        every { sqliteEnabled.isDatabaseCacheEnabled() } answers { callOriginal() }

        // Given - Disabled
        val disabled = mockk<LocalProperties>()
        every { disabled.getDbCacheType() } returns DatabaseType.NONE
        every { disabled.isDatabaseCacheEnabled() } answers { callOriginal() }

        // When/Then
        assertThat(postgresEnabled.isDatabaseCacheEnabled()).isTrue()
        assertThat(sqliteEnabled.isDatabaseCacheEnabled()).isTrue()
        assertThat(disabled.isDatabaseCacheEnabled()).isFalse()
    }

    @Test
    fun `local properties defaults to SQLITE database cache type`() {
        val defaultProperties = mockk<LocalProperties>()
        every { defaultProperties.getProperty("db_cache_type") } returns null
        every { defaultProperties.getDbCacheUrl() } returns null
        every { defaultProperties.getDbCacheSqliteFile() } returns "github-stats-cache.db"
        every { defaultProperties.getDbCacheType() } answers { callOriginal() }
        every { defaultProperties.isDatabaseCacheEnabled() } answers { callOriginal() }

        assertThat(defaultProperties.getDbCacheType()).isEqualTo(DatabaseType.SQLITE)
        assertThat(defaultProperties.isDatabaseCacheEnabled()).isTrue()
    }

    @Test
    fun `sqlite database cache service stores and retrieves cached responses`() =
        runBlocking {
            val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
            SqliteDatabase.Schema.create(driver)
            val database = SqliteDatabase(driver)
            val cacheService = SqliteDatabaseCacheService(database, expirationHours = 24L)

            val testUrl = "https://api.github.com/repos/square/okhttp/pulls/100"
            val testJson = """{"id": 100, "title": "Test PR"}"""

            // Verify miss initially
            val initial = cacheService.getCachedResponse(testUrl)
            assertThat(initial).isNull()

            // Cache response
            cacheService.cacheResponse(testUrl, testJson, 200)

            // Verify hit
            val cached = cacheService.getCachedResponse(testUrl)
            assertThat(cached).isEqualTo(testJson)

            // Check cache stats
            val stats = cacheService.getCacheStats()
            assertThat(stats).isNotNull()
            assertThat(stats!!.totalEntries).isEqualTo(1)
            assertThat(stats.validEntries).isEqualTo(1)
            assertThat(stats.expiredEntries).isEqualTo(0)
        }

    @Test
    fun `database manager initializes sqlite database cache service successfully`() {
        val tempDbFile = File.createTempFile("github-stats-test", ".db")
        tempDbFile.deleteOnExit()

        val localProperties = mockk<LocalProperties>()
        every { localProperties.isDatabaseCacheEnabled() } returns true
        every { localProperties.getDbCacheType() } returns DatabaseType.SQLITE
        every { localProperties.getDbCacheSqliteFile() } returns tempDbFile.absolutePath
        every { localProperties.getDbCacheExpirationHours() } returns 24L

        val service = DatabaseManager.createDatabaseCacheService(localProperties)
        assertThat(service).isNotNull()
        assertThat(service).isInstanceOf(SqliteDatabaseCacheService::class.java)

        DatabaseManager.closeDatabase()
    }

    @Test
    fun `DatabaseType fromString parses correctly`() {
        assertThat(DatabaseType.fromString("SQLITE")).isEqualTo(DatabaseType.SQLITE)
        assertThat(DatabaseType.fromString("sqlite3")).isEqualTo(DatabaseType.SQLITE)
        assertThat(DatabaseType.fromString("POSTGRESQL")).isEqualTo(DatabaseType.POSTGRESQL)
        assertThat(DatabaseType.fromString("postgres")).isEqualTo(DatabaseType.POSTGRESQL)
        assertThat(DatabaseType.fromString("NONE")).isEqualTo(DatabaseType.NONE)
        assertThat(DatabaseType.fromString("disabled")).isEqualTo(DatabaseType.NONE)
        assertThat(DatabaseType.fromString(null)).isEqualTo(DatabaseType.NONE)
    }
}
