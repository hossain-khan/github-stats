package dev.hossain.githubstats.model.timeline

/**
 * Fallback timeline event, that are not used for stats purpose.
 */
data class UnknownEvent(
    override val eventType: String = "UNKNOWN"
) : TimelineEvent
