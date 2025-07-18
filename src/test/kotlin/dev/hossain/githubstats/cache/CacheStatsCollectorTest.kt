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
        assertEquals(4, stats.totalRequests) // databaseCacheHits(2) + okHttpCacheHits(1) + networkRequests(1)
        assertEquals(2, stats.databaseCacheHits)
        assertEquals(1, stats.databaseCacheMisses)
        assertEquals(1, stats.okHttpCacheHits)
        assertEquals(1, stats.networkRequests)

        // Test cache hit rates (based on totalRequests = 4)
        assertEquals(50.0, stats.databaseCacheHitRate, 0.1) // 2/4 = 50%
        assertEquals(25.0, stats.okHttpCacheHitRate, 0.1) // 1/4 = 25%
        assertEquals(75.0, stats.overallCacheHitRate, 0.1) // (2+1)/4 = 75%
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

    @Test
    fun `test real scenario - database hits vs network requests should sum correctly`() {
        val collector = CacheStatsCollector()

        // Simulate scenario from the issue: 656 database hits and 656 network requests
        // This represents 1312 total API requests, not 1968 (which would be double counting)
        repeat(656) {
            collector.recordDatabaseCacheHit()
        }

        repeat(656) {
            collector.recordDatabaseCacheMiss() // These misses...
            collector.recordNetworkRequest() // ...become network requests
        }

        val stats = collector.getStats()

        // Total should be 1312 (656 db hits + 656 network), not 1968 (656 + 656 + 656)
        assertEquals(1312, stats.totalRequests)
        assertEquals(656, stats.databaseCacheHits)
        assertEquals(656, stats.databaseCacheMisses) // Recorded but not counted in total
        assertEquals(0, stats.okHttpCacheHits)
        assertEquals(656, stats.networkRequests)

        // Verify percentages are correct (50% each)
        assertEquals(50.0, stats.databaseCacheHitRate, 0.1)
        assertEquals(0.0, stats.okHttpCacheHitRate, 0.1)
        assertEquals(50.0, stats.networkRequestRate, 0.1)
        assertEquals(50.0, stats.overallCacheHitRate, 0.1) // Only database hits count as cache hits
    }

    @Test
    fun `test PR cache status tracking and summary generation`() {
        val collector = CacheStatsCollector()

        // Test URLs from the issue examples
        val prInfoUrl = "https://api.github.com/repos/square/okhttp/pulls/8796"
        val timelineUrl = "https://api.github.com/repos/square/okhttp/issues/8796/timeline?page=1&per_page=100"
        val commentsUrl = "https://api.github.com/repos/square/okhttp/pulls/8796/comments?page=1&per_page=100"

        // Simulate the cache hit scenario from the issue
        collector.recordDatabaseCacheHit(prInfoUrl)
        collector.recordDatabaseCacheMiss(timelineUrl)
        collector.recordDatabaseCacheHit(commentsUrl)

        // Get the summary and verify it matches expected format
        val summary = collector.getPrCacheStatusAndReset()
        assertEquals("Database cache status (PR info: HIT, Timeline: MISS, Comments: HIT)", summary)

        // Test that reset worked - next call should return null
        val summaryAfterReset = collector.getPrCacheStatusAndReset()
        assertEquals(null, summaryAfterReset)

        // Test partial coverage (only PR info)
        collector.recordDatabaseCacheHit(prInfoUrl)
        val partialSummary = collector.getPrCacheStatusAndReset()
        assertEquals("Database cache status (PR info: HIT)", partialSummary)

        // Test all miss scenario
        collector.recordDatabaseCacheMiss(prInfoUrl)
        collector.recordDatabaseCacheMiss(timelineUrl)
        collector.recordDatabaseCacheMiss(commentsUrl)
        val allMissSummary = collector.getPrCacheStatusAndReset()
        assertEquals("Database cache status (PR info: MISS, Timeline: MISS, Comments: MISS)", allMissSummary)
    }

    @Test
    fun `test PR cache status with non-relevant URLs`() {
        val collector = CacheStatsCollector()

        // URLs that don't match our patterns
        collector.recordDatabaseCacheHit("https://api.github.com/repos/square/okhttp")
        collector.recordDatabaseCacheHit("https://api.github.com/user")

        // Should return null since no relevant URLs were tracked
        val summary = collector.getPrCacheStatusAndReset()
        assertEquals(null, summary)
    }
}
