package dev.hossain.githubstats.cache

/**
 * Demo application to showcase cache statistics collection and formatting.
 */
fun main() {
    println("🧪 Cache Statistics Demo")
    println("=".repeat(50))

    val collector = CacheStatsCollector()
    val formatter = CacheStatsFormatter()

    println("\n1. Initial state (no requests):")
    println(formatter.formatCacheStats(collector.getStats()))

    // Simulate some database cache hits and misses
    repeat(5) { collector.recordDatabaseCacheHit() }
    repeat(2) { collector.recordDatabaseCacheMiss() }
    repeat(3) { collector.recordOkHttpCacheHit() }
    repeat(4) { collector.recordNetworkRequest() }

    println("\n2. After simulating 14 API requests:")
    println(formatter.formatCacheStats(collector.getStats()))

    // Reset and show clean state
    collector.reset()
    println("\n3. After reset:")
    println(formatter.formatCacheStats(collector.getStats()))

    // Simulate poor cache performance
    repeat(1) { collector.recordDatabaseCacheHit() }
    repeat(1) { collector.recordOkHttpCacheHit() }
    repeat(8) { collector.recordNetworkRequest() }

    println("\n4. Simulating poor cache performance (20% hit rate):")
    println(formatter.formatCacheStats(collector.getStats()))

    println("\n✅ Cache statistics demo completed!")
}
