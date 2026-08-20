package dev.hossain.githubstats.cache

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

/**
 * Tests for [CacheStatsFormatter].
 */
class CacheStatsFormatterTest {
    private val formatter = CacheStatsFormatter()

    @Test
    fun `formatCacheStats returns no requests message when totalRequests is zero`() {
        val stats =
            CachePerformanceStats(
                databaseCacheHits = 0L,
                databaseCacheMisses = 0L,
                okHttpCacheHits = 0L,
                networkRequests = 0L,
            )

        val result = formatter.formatCacheStats(stats)
        assertThat(result).isEqualTo("📊 Cache Performance: No API requests were made during this session.")
    }

    @Test
    fun `formatCacheStats formats table and excellent performance recommendation`() {
        val stats =
            CachePerformanceStats(
                databaseCacheHits = 60L,
                databaseCacheMisses = 40L,
                okHttpCacheHits = 25L,
                networkRequests = 15L,
            )

        val result = formatter.formatCacheStats(stats)
        assertThat(result).contains("📊 Cache Performance Statistics")
        assertThat(result).contains("Total API Requests")
        assertThat(result).contains("100")
        assertThat(result).contains("Database Cache Hits")
        assertThat(result).contains("60 (60.0%)")
        assertThat(result).contains("OkHttp Cache Hits")
        assertThat(result).contains("25 (25.0%)")
        assertThat(result).contains("Overall Cache Effectiveness")
        assertThat(result).contains("85.0%")
        assertThat(result).contains("Excellent cache performance!")
        assertThat(result).contains("Database cache is working effectively with 60 hits.")
        assertThat(result).contains("OkHttp cache provided 25 cached responses.")
    }

    @Test
    fun `formatCacheStats formats good cache performance recommendation`() {
        val stats =
            CachePerformanceStats(
                databaseCacheHits = 40L,
                databaseCacheMisses = 60L,
                okHttpCacheHits = 25L,
                networkRequests = 35L,
            )

        val result = formatter.formatCacheStats(stats)
        assertThat(result).contains("Good cache performance. Consider optimizing cache expiration settings.")
    }

    @Test
    fun `formatCacheStats formats moderate cache performance recommendation`() {
        val stats =
            CachePerformanceStats(
                databaseCacheHits = 30L,
                databaseCacheMisses = 70L,
                okHttpCacheHits = 15L,
                networkRequests = 55L,
            )

        val result = formatter.formatCacheStats(stats)
        assertThat(result).contains("Moderate cache performance. Many requests are hitting the network.")
        assertThat(result).contains("Consider increasing cache expiration times to reduce network requests.")
    }

    @Test
    fun `formatCacheStats formats low cache performance recommendation`() {
        val stats =
            CachePerformanceStats(
                databaseCacheHits = 10L,
                databaseCacheMisses = 90L,
                okHttpCacheHits = 10L,
                networkRequests = 80L,
            )

        val result = formatter.formatCacheStats(stats)
        assertThat(result).contains("Low cache performance. Consider reviewing cache configuration.")
        assertThat(result).contains("Consider increasing cache expiration times to reduce network requests.")
    }

    @Test
    fun `logCacheStats logs formatted output without throwing`() {
        val stats =
            CachePerformanceStats(
                databaseCacheHits = 5L,
                databaseCacheMisses = 5L,
                okHttpCacheHits = 2L,
                networkRequests = 3L,
            )

        formatter.logCacheStats(stats)
    }
}
