package dev.hossain.githubstats.model.timeline

/**
 * Base class for all the GitHub timeline events.
 * See [Timeline API](https://docs.github.com/en/rest/issues/timeline) for more info.
 */
sealed interface TimelineEvent {
    /**
     * The type of timeline event that determines the event object.
     */
    val eventType: String
}
