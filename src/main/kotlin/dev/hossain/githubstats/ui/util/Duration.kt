package dev.hossain.githubstats.ui.util

/**
 * Simple duration class for use in the UI.
 * This acts as an adapter between the core application and the UI.
 */
class Duration(val seconds: Long) {
    companion object {
        val ZERO = Duration(0)
    }
    
    fun toSeconds(): Long = seconds
}

/**
 * Convert kotlin.time.Duration to our UI Duration class.
 */
fun kotlin.time.Duration.toUiDuration(): Duration {
    return Duration(this.inWholeSeconds)
}