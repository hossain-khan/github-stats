package dev.hossain.githubstats

import dev.hossain.githubstats.model.PullRequest
import kotlinx.datetime.Instant
import kotlin.time.Duration

data class PrStats(
    val pullRequest: PullRequest,
    val reviewTime: Map<String, Duration>,
    val prReadyOn: Instant,
    val prMergedOn: Instant
)
