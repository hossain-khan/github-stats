package dev.hossain.githubstats.model.timeline

data class UnknownEvent(override val eventType: String = "UNKNOWN") : TimelineEvent
