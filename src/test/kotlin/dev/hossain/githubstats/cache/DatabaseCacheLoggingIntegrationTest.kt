package dev.hossain.githubstats.cache

import dev.hossain.githubstats.BuildConfig
import dev.hossain.githubstats.logging.Log
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

/**
 * Integration test to verify that the database cache logging improvements work end-to-end.
 */
class DatabaseCacheLoggingIntegrationTest {
    @BeforeEach
    fun setup() {
        // Set log level to VERBOSE to capture all log messages
        BuildConfig.logLevel = Log.VERBOSE
    }

    @Test
    fun `test database cache logging levels and summary generation`() {
        val cacheStatsCollector = CacheStatsCollector()

        // Simulate PR analysis with mixed cache hits and misses
        val prInfoUrl = "https://api.github.com/repos/square/okhttp/pulls/8796"
        val timelineUrl = "https://api.github.com/repos/square/okhttp/issues/8796/timeline?page=1&per_page=100"
        val commentsUrl = "https://api.github.com/repos/square/okhttp/pulls/8796/comments?page=1&per_page=100"

        // Test cache hit scenario
        cacheStatsCollector.recordDatabaseCacheHit(prInfoUrl)
        cacheStatsCollector.recordDatabaseCacheMiss(timelineUrl)
        cacheStatsCollector.recordDatabaseCacheHit(commentsUrl)

        // Verify summary generation
        val summary = cacheStatsCollector.getPrCacheStatusAndReset()
        assertTrue(summary != null, "Summary should not be null")
        assertTrue(summary.contains("💾 Database usage cache status for APIs"), "Summary should contain the expected prefix")
        assertTrue(summary.contains("PR info: HIT"), "Summary should contain PR info status")
        assertTrue(summary.contains("Timeline: MISS"), "Summary should contain Timeline status")
        assertTrue(summary.contains("Comments: HIT"), "Summary should contain Comments status")

        println("✅ Generated summary: $summary")

        // Test that summary is reset after retrieval
        val secondSummary = cacheStatsCollector.getPrCacheStatusAndReset()
        assertTrue(secondSummary == null, "Summary should be null after reset")

        // Test all cache hits scenario
        cacheStatsCollector.recordDatabaseCacheHit(prInfoUrl)
        cacheStatsCollector.recordDatabaseCacheHit(timelineUrl)
        cacheStatsCollector.recordDatabaseCacheHit(commentsUrl)

        val allHitsSummary = cacheStatsCollector.getPrCacheStatusAndReset()
        assertTrue(allHitsSummary == "💾 Database usage cache status for APIs (PR info: HIT, Timeline: HIT, Comments: HIT)")

        println("✅ All hits summary: $allHitsSummary")

        // Test all cache misses scenario
        cacheStatsCollector.recordDatabaseCacheMiss(prInfoUrl)
        cacheStatsCollector.recordDatabaseCacheMiss(timelineUrl)
        cacheStatsCollector.recordDatabaseCacheMiss(commentsUrl)

        val allMissesSummary = cacheStatsCollector.getPrCacheStatusAndReset()
        assertTrue(allMissesSummary == "💾 Database usage cache status for APIs (PR info: MISS, Timeline: MISS, Comments: MISS)")

        println("✅ All misses summary: $allMissesSummary")

        // Test partial coverage scenario
        cacheStatsCollector.recordDatabaseCacheHit(prInfoUrl)

        val partialSummary = cacheStatsCollector.getPrCacheStatusAndReset()
        assertTrue(partialSummary == "💾 Database usage cache status for APIs (PR info: HIT)")

        println("✅ Partial summary: $partialSummary")

        println("✅ All integration tests passed!")
    }
}
