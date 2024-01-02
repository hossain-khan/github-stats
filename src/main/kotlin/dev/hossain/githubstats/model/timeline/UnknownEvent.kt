package dev.hossain.githubstats.model.timeline

/**
 * Fallback timeline event, that are not used for stats purpose.
 * @see TimelineEvent
 */
data class UnknownEvent(
    override val eventType: String = "UNKNOWN",
) : TimelineEvent
