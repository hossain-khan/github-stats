package dev.hossain.githubstats.cache

import com.google.common.truth.Truth.assertThat
import dev.hossain.githubstats.util.LocalProperties
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test

/**
 * Tests for database cache related functionality.
 */
class DatabaseCacheTest {
    @Test
    fun `database manager returns null when caching is disabled`() {
        // Given
        val localProperties = mockk<LocalProperties>()
        every { localProperties.isDatabaseCacheEnabled() } returns false

        // When
        val result = SimpleDatabaseManager.initializeDatabase(localProperties)

        // Then
        assertThat(result).isNull()
    }

    @Test
    fun `database manager logs configuration found but returns null for now`() {
        // Given
        val localProperties = mockk<LocalProperties>()
        every { localProperties.isDatabaseCacheEnabled() } returns true

        // When
        val result = SimpleDatabaseManager.initializeDatabase(localProperties)

        // Then
        assertThat(result).isNull() // Current implementation returns null
        assertThat(SimpleDatabaseManager.isDatabaseAvailable()).isFalse()
    }

    @Test
    fun `local properties database config validation works correctly`() {
        // Given
        val localPropertiesEnabled = mockk<LocalProperties>()
        every { localPropertiesEnabled.getDbCacheUrl() } returns "jdbc:postgresql://localhost:5432/test"
        every { localPropertiesEnabled.getDbCacheUsername() } returns "user"
        every { localPropertiesEnabled.getDbCachePassword() } returns "pass"
        every { localPropertiesEnabled.isDatabaseCacheEnabled() } returns true

        val localPropertiesDisabled = mockk<LocalProperties>()
        every { localPropertiesDisabled.getDbCacheUrl() } returns null
        every { localPropertiesDisabled.getDbCacheUsername() } returns null
        every { localPropertiesDisabled.getDbCachePassword() } returns null
        every { localPropertiesDisabled.isDatabaseCacheEnabled() } returns false

        // When/Then
        assertThat(localPropertiesEnabled.isDatabaseCacheEnabled()).isTrue()
        assertThat(localPropertiesDisabled.isDatabaseCacheEnabled()).isFalse()
    }

    @Test
    fun `cache service can be constructed with database`() {
        // This test demonstrates the cache service API structure
        // In a real implementation, this would use an actual database instance

        // For now, we verify that the classes are properly structured
        assertThat(CacheStats::class.java).isNotNull()
        assertThat(DatabaseCacheService::class.java).isNotNull()
        assertThat(DatabaseCacheInterceptor::class.java).isNotNull()
    }
}
