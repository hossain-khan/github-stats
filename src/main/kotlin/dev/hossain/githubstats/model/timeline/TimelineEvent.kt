package dev.hossain.githubstats.model.timeline

/**
 * Base class for all the GitHub timeline events.
 */
sealed interface TimelineEvent {
    /**
     * The type of timeline event that determines the event object.
     */
    val eventType: String
}
