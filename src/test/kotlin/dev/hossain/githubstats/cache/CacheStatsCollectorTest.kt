package dev.hossain.githubstats.cache

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Test for CacheStatsCollector to ensure cache statistics are tracked correctly.
 */
class CacheStatsCollectorTest {
    @Test
    fun `test cache stats tracking`() {
        val collector = CacheStatsCollector()

        // Initially all stats should be zero
        val initialStats = collector.getStats()
        assertEquals(0, initialStats.totalRequests)
        assertEquals(0.0, initialStats.overallCacheHitRate)

        // Record some cache hits and misses
        collector.recordDatabaseCacheHit()
        collector.recordDatabaseCacheHit()
        collector.recordDatabaseCacheMiss()
        collector.recordOkHttpCacheHit()
        collector.recordNetworkRequest()

        val stats = collector.getStats()
        assertEquals(5, stats.totalRequests)
        assertEquals(2, stats.databaseCacheHits)
        assertEquals(1, stats.databaseCacheMisses)
        assertEquals(1, stats.okHttpCacheHits)
        assertEquals(1, stats.networkRequests)

        // Test cache hit rates
        assertEquals(40.0, stats.databaseCacheHitRate, 0.1)
        assertEquals(20.0, stats.okHttpCacheHitRate, 0.1)
        assertEquals(60.0, stats.overallCacheHitRate, 0.1)
    }

    @Test
    fun `test reset functionality`() {
        val collector = CacheStatsCollector()

        // Record some stats
        collector.recordDatabaseCacheHit()
        collector.recordNetworkRequest()

        assertTrue(collector.getStats().totalRequests > 0)

        // Reset should clear everything
        collector.reset()

        val stats = collector.getStats()
        assertEquals(0, stats.totalRequests)
        assertEquals(0, stats.databaseCacheHits)
        assertEquals(0, stats.networkRequests)
    }
}
