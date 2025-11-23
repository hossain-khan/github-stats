package dev.hossain.githubstats.util

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

class PropertiesReaderTest {
    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `getDbCacheUrl returns null when property is not set`() {
        // Given
        val propertiesFile = createPropertiesFile("")
        val localProperties = TestLocalProperties(propertiesFile.absolutePath)

        // When
        val result = localProperties.getDbCacheUrl()

        // Then
        assertThat(result).isNull()
    }

    @Test
    fun `getDbCacheUrl returns valid URL when property is correctly formatted`() {
        // Given
        val validUrl = "jdbc:postgresql://localhost:5432/github_stats_cache"
        val propertiesFile = createPropertiesFile("db_cache_url=$validUrl")
        val localProperties = TestLocalProperties(propertiesFile.absolutePath)

        // When
        val result = localProperties.getDbCacheUrl()

        // Then
        assertThat(result).isEqualTo(validUrl)
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
            // Given
            val propertiesFile = createPropertiesFile("db_cache_url=$validUrl")
            val localProperties = TestLocalProperties(propertiesFile.absolutePath)

            // When & Then
            assertThat(localProperties.getDbCacheUrl()).isEqualTo(validUrl)
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
            // Given
            val propertiesFile = createPropertiesFile("db_cache_url=$validUrl")
            val localProperties = TestLocalProperties(propertiesFile.absolutePath)

            // When & Then
            assertThat(localProperties.getDbCacheUrl()).isEqualTo(validUrl)
        }
    }

    @Test
    fun `getDbCacheUrl throws exception for invalid URL - wrong database type`() {
        // Given
        val invalidUrl = "jdbc:mysql://localhost:3306/github_stats_cache"
        val propertiesFile = createPropertiesFile("db_cache_url=$invalidUrl")
        val localProperties = TestLocalProperties(propertiesFile.absolutePath)

        // When & Then
        val exception =
            assertThrows<IllegalArgumentException> {
                localProperties.getDbCacheUrl()
            }
        assertThat(exception.message).contains("Invalid PostgreSQL JDBC URL format")
        assertThat(exception.message).contains("jdbc:postgresql://host[:port]/database")
    }

    @Test
    fun `getDbCacheUrl throws exception for invalid URL - missing jdbc prefix`() {
        // Given
        val invalidUrl = "postgresql://localhost:5432/github_stats_cache"
        val propertiesFile = createPropertiesFile("db_cache_url=$invalidUrl")
        val localProperties = TestLocalProperties(propertiesFile.absolutePath)

        // When & Then
        val exception =
            assertThrows<IllegalArgumentException> {
                localProperties.getDbCacheUrl()
            }
        assertThat(exception.message).contains("Invalid PostgreSQL JDBC URL format")
    }

    @Test
    fun `getDbCacheUrl throws exception for invalid URL - missing database path separator`() {
        // Given
        val invalidUrl = "jdbc:postgresql://localhost:5432"
        val propertiesFile = createPropertiesFile("db_cache_url=$invalidUrl")
        val localProperties = TestLocalProperties(propertiesFile.absolutePath)

        // When & Then
        val exception =
            assertThrows<IllegalArgumentException> {
                localProperties.getDbCacheUrl()
            }
        assertThat(exception.message).contains("Invalid PostgreSQL JDBC URL format")
    }

    @Test
    fun `getDbCacheUrl throws exception for invalid URL - non-numeric port`() {
        // Given
        val invalidUrl = "jdbc:postgresql://localhost:abc/github_stats_cache"
        val propertiesFile = createPropertiesFile("db_cache_url=$invalidUrl")
        val localProperties = TestLocalProperties(propertiesFile.absolutePath)

        // When & Then
        val exception =
            assertThrows<IllegalArgumentException> {
                localProperties.getDbCacheUrl()
            }
        assertThat(exception.message).contains("Invalid PostgreSQL JDBC URL format")
    }

    @Test
    fun `getDbCacheUrl throws exception for invalid URL - missing database name`() {
        // Given
        val invalidUrl = "jdbc:postgresql://localhost:5432/"
        val propertiesFile = createPropertiesFile("db_cache_url=$invalidUrl")
        val localProperties = TestLocalProperties(propertiesFile.absolutePath)

        // When & Then
        val exception =
            assertThrows<IllegalArgumentException> {
                localProperties.getDbCacheUrl()
            }
        assertThat(exception.message).contains("Invalid PostgreSQL JDBC URL format")
    }

    @Test
    fun `getDbCacheUrl throws exception for invalid URL - invalid characters in database name`() {
        // Given
        val invalidUrl = "jdbc:postgresql://localhost:5432/database-with-hyphens"
        val propertiesFile = createPropertiesFile("db_cache_url=$invalidUrl")
        val localProperties = TestLocalProperties(propertiesFile.absolutePath)

        // When & Then
        val exception =
            assertThrows<IllegalArgumentException> {
                localProperties.getDbCacheUrl()
            }
        assertThat(exception.message).contains("Invalid PostgreSQL JDBC URL format")
    }

    @Test
    fun `getDbCacheUrl throws exception for empty URL`() {
        // Given
        val propertiesFile = createPropertiesFile("db_cache_url=")
        val localProperties = TestLocalProperties(propertiesFile.absolutePath)

        // When & Then
        val exception =
            assertThrows<IllegalArgumentException> {
                localProperties.getDbCacheUrl()
            }
        assertThat(exception.message).contains("Invalid PostgreSQL JDBC URL format")
    }

    @Test
    fun `getDbCacheUrl accepts database names with underscores and numbers`() {
        val validUrls =
            listOf(
                "jdbc:postgresql://localhost:5432/github_stats_cache",
                "jdbc:postgresql://localhost:5432/db123",
                "jdbc:postgresql://localhost:5432/my_database_2024",
                "jdbc:postgresql://localhost:5432/test_db_v1",
            )

        validUrls.forEach { validUrl ->
            // Given
            val propertiesFile = createPropertiesFile("db_cache_url=$validUrl")
            val localProperties = TestLocalProperties(propertiesFile.absolutePath)

            // When & Then
            assertThat(localProperties.getDbCacheUrl()).isEqualTo(validUrl)
        }
    }

    @Test
    fun `getGhCliTimeoutSeconds returns default 30 when property is not set`() {
        // Given
        val propertiesFile = createPropertiesFile("")
        val localProperties = TestLocalProperties(propertiesFile.absolutePath)

        // When
        val result = localProperties.getGhCliTimeoutSeconds()

        // Then
        assertThat(result).isEqualTo(30L)
    }

    @Test
    fun `getGhCliTimeoutSeconds returns configured value when property is set`() {
        // Given
        val propertiesFile = createPropertiesFile("gh_cli_timeout_seconds=60")
        val localProperties = TestLocalProperties(propertiesFile.absolutePath)

        // When
        val result = localProperties.getGhCliTimeoutSeconds()

        // Then
        assertThat(result).isEqualTo(60L)
    }

    @Test
    fun `getGhCliTimeoutSeconds returns default when property is invalid`() {
        // Given
        val propertiesFile = createPropertiesFile("gh_cli_timeout_seconds=invalid")
        val localProperties = TestLocalProperties(propertiesFile.absolutePath)

        // When
        val result = localProperties.getGhCliTimeoutSeconds()

        // Then - Should fall back to default
        assertThat(result).isEqualTo(30L)
    }

    @Test
    fun `getGhCliTimeoutSeconds accepts zero timeout`() {
        // Given
        val propertiesFile = createPropertiesFile("gh_cli_timeout_seconds=0")
        val localProperties = TestLocalProperties(propertiesFile.absolutePath)

        // When
        val result = localProperties.getGhCliTimeoutSeconds()

        // Then
        assertThat(result).isEqualTo(0L)
    }

    @Test
    fun `getGhCliTimeoutSeconds accepts large timeout values`() {
        // Given
        val propertiesFile = createPropertiesFile("gh_cli_timeout_seconds=3600")
        val localProperties = TestLocalProperties(propertiesFile.absolutePath)

        // When
        val result = localProperties.getGhCliTimeoutSeconds()

        // Then
        assertThat(result).isEqualTo(3600L)
    }

    private fun createPropertiesFile(content: String): File {
        val file = tempDir.resolve("test.properties").toFile()
        file.writeText(content)
        return file
    }

    /**
     * Test implementation of LocalProperties that uses a custom file path
     */
    private class TestLocalProperties(
        filePath: String,
    ) : PropertiesReader(filePath) {
        fun getDbCacheUrl(): String? {
            val url = getProperty("db_cache_url")
            if (url != null) {
                validatePostgreSqlUrl(url)
            }
            return url
        }

        fun getGhCliTimeoutSeconds(): Long = getProperty("gh_cli_timeout_seconds")?.toLongOrNull() ?: 30L

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
}
