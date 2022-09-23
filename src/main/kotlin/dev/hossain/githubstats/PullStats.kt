package dev.hossain.githubstats

import dev.hossain.githubstats.BuildConfig.REPO_ID
import dev.hossain.githubstats.BuildConfig.REPO_OWNER
import dev.hossain.githubstats.model.User
import dev.hossain.githubstats.model.timeline.MergedEvent
import dev.hossain.githubstats.model.timeline.ReadyForReviewEvent
import dev.hossain.githubstats.model.timeline.ReviewRequestedEvent
import dev.hossain.githubstats.model.timeline.ReviewedEvent
import dev.hossain.githubstats.model.timeline.TimelineEvent
import dev.hossain.githubstats.service.GithubService
import kotlinx.datetime.Instant
import kotlinx.datetime.toInstant
import kotlinx.datetime.toJavaInstant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import kotlin.time.Duration

class PullStats(private val githubService: GithubService) {

    private val dateFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
        .withLocale(Locale.US)
        .withZone(ZoneId.systemDefault())

    sealed class StatsResult {
        data class Success(val data: String)
        object Failure : StatsResult()
    }

    suspend fun calculateStats(prNumber: Int): StatsResult {
        val pullRequest = githubService.pullRequest(REPO_OWNER, REPO_ID, prNumber)
        val pullTimelineEvents = githubService.timelineEvents(REPO_OWNER, REPO_ID, prNumber)

        val mergedEvent: MergedEvent = pullTimelineEvents.find { it.eventType == MergedEvent.TYPE } as MergedEvent?
            ?: throw IllegalStateException("PR has not been merged, no reason to check stats.")

        val prCreatedOn = pullRequest.created_at.toInstant()
        val prAvailableForReview = prAvailableForReviewTime(prCreatedOn, pullTimelineEvents)
        val prReviewers: Set<User> = prReviewers(pullTimelineEvents)
        val reviewCompletionInfo = reviewTimeByUser(prAvailableForReview, prReviewers, pullTimelineEvents)

        println("- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -")

        println("PR Available: ${dateFormatter.format(prAvailableForReview.toJavaInstant())}")
        println("Review Time: $reviewCompletionInfo")
        println(
            "PR Merged in: ${mergedEvent.created_at.toInstant() - prAvailableForReview} on ${
            dateFormatter.format(
                mergedEvent.created_at.toInstant().toJavaInstant()
            )
            }"
        )

        println("- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -")

        return StatsResult.Failure
    }

    private fun reviewTimeByUser(
        prAvailableForReview: Instant,
        prReviewers: Set<User>,
        pullTimelineEvents: List<TimelineEvent>
    ): Map<String, Duration> {
        val reviewTimesByUser = mutableMapOf<String, Duration>()

        prReviewers.forEach { reviewer ->
            // Find out if user has been requested to review later
            val requestedLater = pullTimelineEvents.asSequence().filter { it.eventType == ReviewRequestedEvent.TYPE }
                .map { it as ReviewRequestedEvent }.any { it.requested_reviewer == reviewer }

            val reviewedByUserEvent: ReviewedEvent =
                pullTimelineEvents.find { it.eventType == ReviewedEvent.TYPE && (it as ReviewedEvent).user == reviewer } as ReviewedEvent
            if (requestedLater) {
                val reviewRequestedEvent =
                    pullTimelineEvents.find { it.eventType == ReviewRequestedEvent.TYPE && (it as ReviewRequestedEvent).requested_reviewer == reviewer } as ReviewRequestedEvent
                reviewTimesByUser[reviewer.login] =
                    reviewedByUserEvent.submitted_at.toInstant() - reviewRequestedEvent.created_at.toInstant()
            } else {
                reviewTimesByUser[reviewer.login] = reviewedByUserEvent.submitted_at.toInstant() - prAvailableForReview
            }
        }

        return reviewTimesByUser
    }

    /**
     * Extracts all the PR reviewers who reviewed or has been requested to review.
     */
    private fun prReviewers(pullTimelineEvents: List<TimelineEvent>): Set<User> {
        return pullTimelineEvents.asSequence().filter { it.eventType == ReviewRequestedEvent.TYPE }
            .map { it as ReviewRequestedEvent }
            .map { it.actor }.plus(
                pullTimelineEvents.asSequence().filter { it.eventType == ReviewedEvent.TYPE }
                    .map { it as ReviewedEvent }
                    .map { it.user }
            ).toSet()
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
