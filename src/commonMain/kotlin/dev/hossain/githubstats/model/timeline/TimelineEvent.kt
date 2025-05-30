package dev.hossain.githubstats.model.timeline

import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.companionObject

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

/**
 * Filters list of timeline events to specific timeline event sub-items.
 *
 * Usage:
 * ```
 * timelineEvents.filterTo(CommentedEvent::class)
 * ```
 */
@Suppress("UNCHECKED_CAST")
fun <T : TimelineEvent> List<TimelineEvent>.filterTo(kClass: KClass<T>): List<T> {
    // Finds the timeline event type value first
    val clazzEventType: String =
        kClass.companionObject!!
            .members
            .filter { it is KProperty }
            .first()
            .call(null) as String

    // Filters the timelines to only selected type, and returns typed list
    return this.filter { it.eventType == clazzEventType }.map { it as T }
}
