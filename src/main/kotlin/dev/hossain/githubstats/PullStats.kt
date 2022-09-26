package dev.hossain.githubstats

import dev.hossain.githubstats.model.User
import dev.hossain.githubstats.model.timeline.ReadyForReviewEvent
import dev.hossain.githubstats.model.timeline.ReviewRequestedEvent
import dev.hossain.githubstats.model.timeline.ReviewedEvent
import dev.hossain.githubstats.model.timeline.TimelineEvent
import dev.hossain.githubstats.service.GithubService
import kotlinx.datetime.Instant
import kotlinx.datetime.toInstant
import kotlin.time.Duration

/**
 * Creates PR stats using combination of data from the PR.
 */
class PullStats(private val githubService: GithubService) {

    sealed class StatsResult {
        data class Success(
            val stats: PrStats
        ) : StatsResult()

        data class Failure(
            val error: Throwable
        ) : StatsResult()
    }

    /**
     * Calculates Pull Request stats for given [prNumber].
     *
     * Example usage:
     * ```kotlin
     * when (val result = pullStats.calculateStats(47550)) {
     *     is PullStats.StatsResult.Failure -> {
     *         println("Got error for stats: ${result.error}")
     *     }
     *     is PullStats.StatsResult.Success -> {
     *         println(formatter.formatPrStats(result.stats))
     *     }
     * }
     * ```
     *
     * Interesting PRs:
     * - https://github.com/square/retrofit/pull/3613
     * - https://github.com/square/retrofit/pull/3267
     * - https://github.com/freeCodeCamp/freeCodeCamp/pull/47594
     * - https://github.com/freeCodeCamp/freeCodeCamp/pull/47550
     */
    suspend fun calculateStats(owner: String, repo: String, prNumber: Int): StatsResult {
        val pullRequest = githubService.pullRequest(owner, repo, prNumber)
        val pullTimelineEvents = githubService.timelineEvents(owner, repo, prNumber)

        if (pullRequest.merged == null || pullRequest.merged == false) {
            return StatsResult.Failure(IllegalStateException("PR has not been merged, no reason to check stats."))
        }

        // Seems like merged event is not a good indicator, see https://github.com/opensearch-project/OpenSearch/pull/4515
        /*val mergedEvent: MergedEvent = pullTimelineEvents.find { it.eventType == MergedEvent.TYPE } as MergedEvent?
            ?: return StatsResult.Failure(IllegalStateException("PR has not been merged, no reason to check stats."))*/

        val prCreatedOn = pullRequest.created_at.toInstant()
        val prAvailableForReview = prAvailableForReviewTime(prCreatedOn, pullTimelineEvents)
        val prReviewers: Set<User> = prReviewers(pullRequest.user, pullTimelineEvents)
        val reviewCompletionInfo: Map<String, Duration> =
            reviewTimeByUser(prAvailableForReview, prReviewers, pullTimelineEvents)

        return StatsResult.Success(
            PrStats(
                pullRequest = pullRequest,
                reviewTime = reviewCompletionInfo,
                prReadyOn = prAvailableForReview,
                prMergedOn = pullRequest.merged_at!!.toInstant()
            )
        )
    }

    /**
     * Provides the time required to approve the PR.
     *
     * NOTE: Future improvement, should provide both metrics:
     * - Time to first review
     * - Turn around time to approve
     */
    private fun reviewTimeByUser(
        prAvailableForReview: Instant,
        prReviewers: Set<User>,
        pullTimelineEvents: List<TimelineEvent>
    ): Map<String, Duration> {
        val reviewTimesByUser = mutableMapOf<String, Duration>()

        prReviewers.forEach { reviewer ->
            // Find out if user has approved the PR, if not, do not include in stats
            val hasApprovedPr = pullTimelineEvents.asSequence().filter { it.eventType == ReviewedEvent.TYPE }
                .map { it as ReviewedEvent }
                .any { it.user == reviewer && it.state == ReviewedEvent.ReviewState.APPROVED }

            if (hasApprovedPr.not()) {
                return@forEach
            }

            // Find out if user has been requested to review later
            val requestedLater = pullTimelineEvents.asSequence().filter { it.eventType == ReviewRequestedEvent.TYPE }
                .map { it as ReviewRequestedEvent }.any { it.requested_reviewer == reviewer }

            val approvedPrEvent: ReviewedEvent =
                pullTimelineEvents.find {
                    it.eventType == ReviewedEvent.TYPE &&
                        (it as ReviewedEvent).user == reviewer &&
                        it.state == ReviewedEvent.ReviewState.APPROVED
                } as ReviewedEvent
            if (requestedLater) {
                val reviewRequestedEvent =
                    pullTimelineEvents.find { it.eventType == ReviewRequestedEvent.TYPE && (it as ReviewRequestedEvent).requested_reviewer == reviewer } as ReviewRequestedEvent
                reviewTimesByUser[reviewer.login] =
                    approvedPrEvent.submitted_at.toInstant() - reviewRequestedEvent.created_at.toInstant()
            } else {
                reviewTimesByUser[reviewer.login] = approvedPrEvent.submitted_at.toInstant() - prAvailableForReview
            }
        }

        return reviewTimesByUser
    }

    /**
     * Extracts all the PR reviewers who reviewed (approved or commented)
     * or has been requested to review.
     */
    private fun prReviewers(
        prAuthor: User,
        pullTimelineEvents: List<TimelineEvent>
    ): Set<User> {
        return pullTimelineEvents.asSequence()
            .filter { it.eventType == ReviewRequestedEvent.TYPE }
            .map { it as ReviewRequestedEvent }
            .map { it.actor }.plus(
                pullTimelineEvents.asSequence()
                    .filter { it.eventType == ReviewedEvent.TYPE }
                    .map { it as ReviewedEvent }
                    .map { it.user }
            ).toSet()
            .minus(prAuthor)
    }

    private fun prAvailableForReviewTime(
        prCreatedOn: Instant,
        pullTimelineEvents: List<TimelineEvent>
    ): Instant {
        val wasDraftPr: Boolean = pullTimelineEvents.any { it.eventType == ReadyForReviewEvent.TYPE }

        if (wasDraftPr) {
            val readyForReview =
                pullTimelineEvents.find { it.eventType == ReadyForReviewEvent.TYPE }!! as ReadyForReviewEvent
            return readyForReview.created_at.toInstant()
        }

        return prCreatedOn // FIXME - this needs more testing
    }
}
