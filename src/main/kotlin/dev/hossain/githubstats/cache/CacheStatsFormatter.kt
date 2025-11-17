package dev.hossain.githubstats.cache

import com.jakewharton.picnic.table
import dev.hossain.githubstats.logging.Log

/**
 * Formatter for cache performance statistics to display cache performance
 * in a human-readable format at the end of stats generation.
 *
 * Sample output:
 * ```
 * 📊 Cache Performance Statistics
 * ══════════════════════════════════════════════════
 * ┌─────────────────────────────┐
 * │ Cache Performance Summary   │
 * ├─────────────────────────────┼───────────┐
 * │ Total API Requests          │ 12        │
 * ├─────────────────────────────┼───────────┤
 * │ Database Cache Hits         │ 5 (41.7%) │
 * ├─────────────────────────────┼───────────┤
 * │ OkHttp Cache Hits           │ 3 (25.0%) │
 * ├─────────────────────────────┼───────────┤
 * │ Network Requests            │ 4 (33.3%) │
 * ├─────────────────────────────┼───────────┤
 * │ Overall Cache Effectiveness │ 66.7%     │
 * └─────────────────────────────┴───────────┘
 *
 * 🔍 Cache Analysis:
 * ✅ Good cache performance. Consider optimizing cache expiration settings.
 * 💾 Database cache is working effectively with 5 hits.
 * 🗂️  OkHttp cache provided 3 cached responses.
 * ```
 */
class CacheStatsFormatter {
    /**
     * Formats cache performance statistics into a readable table format.
     */
    fun formatCacheStats(stats: CachePerformanceStats): String {
        if (stats.totalRequests == 0L) {
            return "📊 Cache Performance: No API requests were made during this session."
        }

        val table =
            table {
                cellStyle {
                    border = true
                    paddingLeft = 1
                    paddingRight = 1
                }
                header {
                    row("Cache Performance Summary")
                }
                body {
                    row("Total API Requests", stats.totalRequests)
                    row("Database Cache Hits", "${stats.databaseCacheHits} (${String.format("%.1f", stats.databaseCacheHitRate)}%)")
                    row("OkHttp Cache Hits", "${stats.okHttpCacheHits} (${String.format("%.1f", stats.okHttpCacheHitRate)}%)")
                    row("Network Requests", "${stats.networkRequests} (${String.format("%.1f", stats.networkRequestRate)}%)")
                    row("Overall Cache Effectiveness", "${String.format("%.1f", stats.overallCacheHitRate)}%")
                }
            }

        return buildString {
            appendLine()
            appendLine("📊 Cache Performance Statistics")
            appendLine("═".repeat(50))
            appendLine(table.toString())
            appendLine()
            appendLine(generateCacheAnalysis(stats))
        }
    }

    /**
     * Generates analysis and recommendations based on cache performance.
     */
    private fun generateCacheAnalysis(stats: CachePerformanceStats): String =
        buildString {
            appendLine("🔍 Cache Analysis:")

            when {
                stats.overallCacheHitRate >= 80 -> {
                    appendLine("✅ Excellent cache performance! Most requests are being served from cache.")
                }

                stats.overallCacheHitRate >= 60 -> {
                    appendLine("✅ Good cache performance. Consider optimizing cache expiration settings.")
                }

                stats.overallCacheHitRate >= 40 -> {
                    appendLine("⚠️  Moderate cache performance. Many requests are hitting the network.")
                }

                else -> {
                    appendLine("⚠️  Low cache performance. Consider reviewing cache configuration.")
                }
            }

            if (stats.databaseCacheHits > 0) {
                appendLine("💾 Database cache is working effectively with ${stats.databaseCacheHits} hits.")
            }

            if (stats.okHttpCacheHits > 0) {
                appendLine("🗂️  OkHttp cache provided ${stats.okHttpCacheHits} cached responses.")
            }

            if (stats.networkRequests > stats.totalRequests * 0.5) {
                appendLine("🌐 Consider increasing cache expiration times to reduce network requests.")
            }
        }

    /**
     * Logs cache statistics using the application's logging framework.
     */
    fun logCacheStats(stats: CachePerformanceStats) {
        Log.i(formatCacheStats(stats))
    }
}
