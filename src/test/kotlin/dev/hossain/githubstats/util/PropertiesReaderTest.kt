package dev.hossain.githubstats.util

import com.google.common.truth.Truth.assertThat
import dev.hossain.githubstats.cache.DatabaseType
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

class PropertiesReaderTest {
    @TempDir
    lateinit var tempDir: Path

    // ==========================================
    // LocalProperties - General App Config Tests
    // ==========================================

    @Test
    fun `getApiClientType returns RETROFIT by default`() {
        val propertiesFile = createPropertiesFile("")
        val properties = TestProperties(propertiesFile.absolutePath)

        assertThat(properties.getApiClientType()).isEqualTo("RETROFIT")
    }

    @Test
    fun `getApiClientType returns configured client type`() {
        val propertiesFile = createPropertiesFile("api_client_type=GH_CLI")
        val properties = TestProperties(propertiesFile.absolutePath)

        assertThat(properties.getApiClientType()).isEqualTo("GH_CLI")
    }

    @Test
    fun `getRepoOwner returns configured owner`() {
        val propertiesFile = createPropertiesFile("repository_owner=testowner")
        val properties = TestProperties(propertiesFile.absolutePath)

        assertThat(properties.getRepoOwner()).isEqualTo("testowner")
    }

    @Test
    fun `getRepoOwner throws exception when missing`() {
        val propertiesFile = createPropertiesFile("")
        val properties = TestProperties(propertiesFile.absolutePath)

        assertThrows<IllegalArgumentException> {
            properties.getRepoOwner()
        }
    }

    @Test
    fun `getRepoId returns configured repo id`() {
        val propertiesFile = createPropertiesFile("repository_id=testrepo")
        val properties = TestProperties(propertiesFile.absolutePath)

        assertThat(properties.getRepoId()).isEqualTo("testrepo")
    }

    @Test
    fun `getRepoId throws exception when missing`() {
        val propertiesFile = createPropertiesFile("")
        val properties = TestProperties(propertiesFile.absolutePath)

        assertThrows<IllegalArgumentException> {
            properties.getRepoId()
        }
    }

    @Test
    fun `getAuthors returns configured authors or null`() {
        val withAuthors = TestProperties(createPropertiesFile("authors=user1,user2").absolutePath)
        val withoutAuthors = TestProperties(createPropertiesFile("").absolutePath)

        assertThat(withAuthors.getAuthors()).isEqualTo("user1,user2")
        assertThat(withoutAuthors.getAuthors()).isNull()
    }

    @Test
    fun `getBotUsers returns configured bot users or null`() {
        val withBots = TestProperties(createPropertiesFile("bot_users=bot1,bot2").absolutePath)
        val withoutBots = TestProperties(createPropertiesFile("").absolutePath)

        assertThat(withBots.getBotUsers()).isEqualTo("bot1,bot2")
        assertThat(withoutBots.getBotUsers()).isNull()
    }

    @Test
    fun `getDateLimits returns configured dates or null`() {
        val withDates =
            TestProperties(
                createPropertiesFile(
                    """
                    date_limit_after=2024-01-01
                    date_limit_before=2024-12-31
                    """.trimIndent(),
                ).absolutePath,
            )
        val withoutDates = TestProperties(createPropertiesFile("").absolutePath)

        assertThat(withDates.getDateLimitAfter()).isEqualTo("2024-01-01")
        assertThat(withDates.getDateLimitBefore()).isEqualTo("2024-12-31")
        assertThat(withoutDates.getDateLimitAfter()).isNull()
        assertThat(withoutDates.getDateLimitBefore()).isNull()
    }

    // ==========================================
    // LocalProperties - Database Cache Tests
    // ==========================================

    @Test
    fun `getDbCacheType defaults to SQLITE when not specified`() {
        val properties = TestProperties(createPropertiesFile("").absolutePath)
        assertThat(properties.getDbCacheType()).isEqualTo(DatabaseType.SQLITE)
    }

    @Test
    fun `getDbCacheType parses explicit configuration`() {
        val sqlite = TestProperties(createPropertiesFile("db_cache_type=SQLITE").absolutePath)
        val postgres = TestProperties(createPropertiesFile("db_cache_type=POSTGRESQL").absolutePath)
        val none = TestProperties(createPropertiesFile("db_cache_type=NONE").absolutePath)

        assertThat(sqlite.getDbCacheType()).isEqualTo(DatabaseType.SQLITE)
        assertThat(postgres.getDbCacheType()).isEqualTo(DatabaseType.POSTGRESQL)
        assertThat(none.getDbCacheType()).isEqualTo(DatabaseType.NONE)
    }

    @Test
    fun `getDbCacheType auto-detects POSTGRESQL when postgres URL provided without explicit type`() {
        val properties =
            TestProperties(
                createPropertiesFile("db_cache_url=jdbc:postgresql://localhost:5432/github_stats_cache").absolutePath,
            )
        assertThat(properties.getDbCacheType()).isEqualTo(DatabaseType.POSTGRESQL)
    }

    @Test
    fun `getDbCacheSqliteFile returns default filename or configured value`() {
        val defaultFile = TestProperties(createPropertiesFile("").absolutePath)
        val customFile = TestProperties(createPropertiesFile("db_cache_sqlite_file=custom.db").absolutePath)

        assertThat(defaultFile.getDbCacheSqliteFile()).isEqualTo("github-stats-cache.db")
        assertThat(customFile.getDbCacheSqliteFile()).isEqualTo("custom.db")
    }

    @Test
    fun `getDbCacheCredentials returns configured username and password or null`() {
        val withCreds =
            TestProperties(
                createPropertiesFile(
                    """
                    db_cache_username=testuser
                    db_cache_password=testpass
                    """.trimIndent(),
                ).absolutePath,
            )
        val withoutCreds = TestProperties(createPropertiesFile("").absolutePath)

        assertThat(withCreds.getDbCacheUsername()).isEqualTo("testuser")
        assertThat(withCreds.getDbCachePassword()).isEqualTo("testpass")
        assertThat(withoutCreds.getDbCacheUsername()).isNull()
        assertThat(withoutCreds.getDbCachePassword()).isNull()
    }

    @Test
    fun `getDbCacheExpirationHours returns default 24 or configured value`() {
        val defaultExpiry = TestProperties(createPropertiesFile("").absolutePath)
        val customExpiry = TestProperties(createPropertiesFile("db_cache_expiration_hours=168").absolutePath)
        val invalidExpiry = TestProperties(createPropertiesFile("db_cache_expiration_hours=invalid").absolutePath)

        assertThat(defaultExpiry.getDbCacheExpirationHours()).isEqualTo(24L)
        assertThat(customExpiry.getDbCacheExpirationHours()).isEqualTo(168L)
        assertThat(invalidExpiry.getDbCacheExpirationHours()).isEqualTo(24L)
    }

    @Test
    fun `isDatabaseCacheEnabled evaluates correctly for SQLite, Postgres, and None`() {
        val sqliteDefault = TestProperties(createPropertiesFile("").absolutePath)
        val sqliteExplicit =
            TestProperties(
                createPropertiesFile(
                    """
                    db_cache_type=SQLITE
                    db_cache_sqlite_file=test.db
                    """.trimIndent(),
                ).absolutePath,
            )
        val postgresValid =
            TestProperties(
                createPropertiesFile(
                    """
                    db_cache_type=POSTGRESQL
                    db_cache_url=jdbc:postgresql://localhost:5432/github_stats_cache
                    db_cache_username=user
                    db_cache_password=pass
                    """.trimIndent(),
                ).absolutePath,
            )
        val postgresIncomplete =
            TestProperties(
                createPropertiesFile(
                    """
                    db_cache_type=POSTGRESQL
                    db_cache_url=jdbc:postgresql://localhost:5432/github_stats_cache
                    """.trimIndent(),
                ).absolutePath,
            )
        val none = TestProperties(createPropertiesFile("db_cache_type=NONE").absolutePath)

        assertThat(sqliteDefault.isDatabaseCacheEnabled()).isTrue()
        assertThat(sqliteExplicit.isDatabaseCacheEnabled()).isTrue()
        assertThat(postgresValid.isDatabaseCacheEnabled()).isTrue()
        assertThat(postgresIncomplete.isDatabaseCacheEnabled()).isFalse()
        assertThat(none.isDatabaseCacheEnabled()).isFalse()
    }

    @Test
    fun `getDbCacheUrl returns null when property is not set`() {
        val properties = TestProperties(createPropertiesFile("").absolutePath)
        assertThat(properties.getDbCacheUrl()).isNull()
    }

    @Test
    fun `getDbCacheUrl returns valid URL when property is correctly formatted`() {
        val validUrl = "jdbc:postgresql://localhost:5432/github_stats_cache"
        val properties = TestProperties(createPropertiesFile("db_cache_url=$validUrl").absolutePath)

        assertThat(properties.getDbCacheUrl()).isEqualTo(validUrl)
    }

    @Test
    fun `getDbCacheUrl accepts valid URLs with different hosts and ports`() {
        val validUrls =
            listOf(
                "jdbc:postgresql://some.example.com:5432/github_stats_cache",
                "jdbc:postgresql://db.company.org:5432/my_database",
                "jdbc:postgresql://192.168.1.100:5432/stats_db",
                "jdbc:postgresql://localhost:3306/test_db",
                "jdbc:postgresql://my-host.local:5432/db_name",
            )

        validUrls.forEach { validUrl ->
            val properties = TestProperties(createPropertiesFile("db_cache_url=$validUrl").absolutePath)
            assertThat(properties.getDbCacheUrl()).isEqualTo(validUrl)
        }
    }

    @Test
    fun `getDbCacheUrl accepts valid URLs without explicit ports`() {
        val validUrls =
            listOf(
                "jdbc:postgresql://ep-falling-fire-ae78lnzi-pooler.c-2.us-east-2.aws.neon.tech/neondb",
                "jdbc:postgresql://localhost/github_stats_cache",
                "jdbc:postgresql://db.example.com/my_database",
                "jdbc:postgresql://some-host.local/test_db",
            )

        validUrls.forEach { validUrl ->
            val properties = TestProperties(createPropertiesFile("db_cache_url=$validUrl").absolutePath)
            assertThat(properties.getDbCacheUrl()).isEqualTo(validUrl)
        }
    }

    @Test
    fun `getDbCacheUrl throws exception for invalid URL formats`() {
        val invalidUrls =
            listOf(
                "jdbc:mysql://localhost:3306/github_stats_cache",
                "postgresql://localhost:5432/github_stats_cache",
                "jdbc:postgresql://localhost:5432",
                "jdbc:postgresql://localhost:abc/github_stats_cache",
                "jdbc:postgresql://localhost:5432/",
                "jdbc:postgresql://localhost:5432/database-with-hyphens",
                "",
            )

        invalidUrls.forEach { invalidUrl ->
            val properties = TestProperties(createPropertiesFile("db_cache_url=$invalidUrl").absolutePath)
            assertThrows<IllegalArgumentException> {
                properties.getDbCacheUrl()
            }
        }
    }

    // ==========================================
    // LocalProperties - GH CLI Config Tests
    // ==========================================

    @Test
    fun `getGhCliTimeoutSeconds returns default 10 when property is not set`() {
        val properties = TestProperties(createPropertiesFile("").absolutePath)
        assertThat(properties.getGhCliTimeoutSeconds()).isEqualTo(10L)
    }

    @Test
    fun `getGhCliTimeoutSeconds returns configured value when property is set`() {
        val properties = TestProperties(createPropertiesFile("gh_cli_timeout_seconds=60").absolutePath)
        assertThat(properties.getGhCliTimeoutSeconds()).isEqualTo(60L)
    }

    @Test
    fun `getGhCliTimeoutSeconds returns default when property is invalid`() {
        val properties = TestProperties(createPropertiesFile("gh_cli_timeout_seconds=invalid").absolutePath)
        assertThat(properties.getGhCliTimeoutSeconds()).isEqualTo(10L)
    }

    @Test
    fun `getGhCliTimeoutSeconds accepts zero timeout`() {
        val properties = TestProperties(createPropertiesFile("gh_cli_timeout_seconds=0").absolutePath)
        assertThat(properties.getGhCliTimeoutSeconds()).isEqualTo(0L)
    }

    @Test
    fun `getGhCliTimeoutSeconds accepts large timeout values`() {
        val properties = TestProperties(createPropertiesFile("gh_cli_timeout_seconds=3600").absolutePath)
        assertThat(properties.getGhCliTimeoutSeconds()).isEqualTo(3600L)
    }

    private fun createPropertiesFile(content: String): File {
        val file = tempDir.resolve("test.properties").toFile()
        file.writeText(content)
        return file
    }

    /**
     * Test implementation that directly subclasses [LocalProperties] with a custom file path.
     */
    private class TestProperties(
        filePath: String,
    ) : LocalProperties(filePath)
}
