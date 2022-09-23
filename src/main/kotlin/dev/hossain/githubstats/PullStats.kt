package dev.hossain.githubstats

import dev.hossain.githubstats.BuildConfig.REPO_ID
import dev.hossain.githubstats.BuildConfig.REPO_OWNER
import dev.hossain.githubstats.model.User
import dev.hossain.githubstats.model.timeline.ReadyForReviewEvent
import dev.hossain.githubstats.model.timeline.ReviewRequestedEvent
import dev.hossain.githubstats.model.timeline.ReviewedEvent
import dev.hossain.githubstats.model.timeline.TimelineEvent
import dev.hossain.githubstats.service.GithubService
import kotlinx.datetime.Instant
import kotlinx.datetime.toInstant

class PullStats(private val githubService: GithubService) {

    sealed class StatsResult {
        data class Success(val data: String)
        object Failure : StatsResult()
    }

    suspend fun calculateStats(): StatsResult {
        val pullRequest = githubService.pullRequest(REPO_OWNER, REPO_ID, 3618)
        val pullTimelineEvents = githubService.timelineEvents(REPO_OWNER, REPO_ID, 3618)

        val prCreatedOn = pullRequest.created_at.toInstant()
        val prAvailableForReview = prAvailableForReviewTime(prCreatedOn, pullTimelineEvents)
        val prReviewers: List<User> = prReviewers(pullTimelineEvents)

        println("- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -")

        println("PR Available: $prAvailableForReview, Reviewers: $prReviewers")

        println("- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -")

        return StatsResult.Failure
    }

    private fun prReviewers(pullTimelineEvents: List<TimelineEvent>): List<User> {
        return pullTimelineEvents.filter { it.eventType == ReviewRequestedEvent.TYPE }
            .map { it as ReviewRequestedEvent }
            .map { it.actor }.plus(
                pullTimelineEvents.filter { it.eventType == ReviewedEvent.TYPE }
                    .map { it as ReviewedEvent }
                    .map { it.user }
            )
    }

    private fun prAvailableForReviewTime(
        prCreatedOn: Instant,
        pullTimelineEvents: List<TimelineEvent>
    ): Instant {
        val wasDraftPr: Boolean = pullTimelineEvents.any { it.eventType == ReadyForReviewEvent.TYPE }

        if (wasDraftPr) {
            val reviewRequestedEvent =
                pullTimelineEvents.find { it.eventType == ReadyForReviewEvent.TYPE }!! as ReviewRequestedEvent
            return reviewRequestedEvent.created_at.toInstant()
        }

        return prCreatedOn // FIXME
    }
}
