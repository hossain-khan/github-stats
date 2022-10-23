package dev.hossain.githubstats.repository

import dev.hossain.githubstats.BuildConfig
import dev.hossain.githubstats.PrStats
import dev.hossain.githubstats.model.PullRequest
import dev.hossain.githubstats.model.User
import dev.hossain.githubstats.model.timeline.ReadyForReviewEvent
import dev.hossain.githubstats.model.timeline.ReviewRequestedEvent
import dev.hossain.githubstats.model.timeline.ReviewedEvent
import dev.hossain.githubstats.model.timeline.TimelineEvent
import dev.hossain.githubstats.repository.PullRequestStatsRepo.StatsResult
import dev.hossain.githubstats.service.GithubService
import dev.hossain.time.DateTimeDiffer
import dev.hossain.time.UserTimeZone
import kotlinx.datetime.Instant
import kotlinx.datetime.toInstant
import kotlin.time.Duration

/**
 * Creates PR stats using combination of data from the PR using [githubService].
 */
class PullRequestStatsRepoImpl(
    private val githubService: GithubService,
    private val userTimeZone: UserTimeZone
) : PullRequestStatsRepo {
    /**
     * Provides Pull Request stats [PrStats] for given [prNumber].
     */
    override suspend fun stats(
        repoOwner: String,
        repoId: String,
        prNumber: Int
    ): StatsResult {
        val pullRequest = githubService.pullRequest(repoOwner, repoId, prNumber)
        val prTimelineEvents = githubService.timelineEvents(repoOwner, repoId, prNumber)

        if (pullRequest.merged == null || pullRequest.merged == false) {
            return StatsResult.Failure(IllegalStateException("PR has not been merged, no reason to check stats."))
        }

        if (BuildConfig.DEBUG) {
            println("\n- Getting PR#$prNumber info. Analyzing ${prTimelineEvents.size} events from the PR. (URL: ${pullRequest.html_url})")
        }

        val prCreatedOn: Instant = pullRequest.created_at.toInstant()
        val prAvailableForReviewOn: Instant = prAvailableForReviewTime(prCreatedOn, prTimelineEvents)
        val prReviewers: Set<User> = prReviewers(pullRequest.user, prTimelineEvents)
        val reviewCompletionInfo: Map<String, Duration> = reviewTimeByUser(
            pullRequest = pullRequest,
            prAvailableForReview = prAvailableForReviewOn,
            prReviewers = prReviewers,
            prTimelineEvents = prTimelineEvents
        )

        return StatsResult.Success(
            PrStats(
                pullRequest = pullRequest,
                reviewTime = reviewCompletionInfo,
                prReadyOn = prAvailableForReviewOn,
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
        pullRequest: PullRequest,
        prAvailableForReview: Instant,
        prReviewers: Set<User>,
        prTimelineEvents: List<TimelineEvent>
    ): Map<String, Duration> {
        val reviewTimesByUser = mutableMapOf<String, Duration>()

        prReviewers.forEach { reviewer ->
            // Find out if user has approved the PR, if not, do not include in stats
            val hasApprovedPr = prTimelineEvents.asSequence().filter { it.eventType == ReviewedEvent.TYPE }
                .map { it as ReviewedEvent }
                .any { it.user == reviewer && it.state == ReviewedEvent.ReviewState.APPROVED }

            if (hasApprovedPr.not()) {
                return@forEach
            }

            // Find out if user has been requested to review later
            val requestedLater = prTimelineEvents.asSequence().filter { it.eventType == ReviewRequestedEvent.TYPE }
                .map { it as ReviewRequestedEvent }.any { it.requested_reviewer == reviewer }

            val approvedPrEvent: ReviewedEvent =
                prTimelineEvents.find {
                    it.eventType == ReviewedEvent.TYPE &&
                        (it as ReviewedEvent).user == reviewer &&
                        it.state == ReviewedEvent.ReviewState.APPROVED
                } as ReviewedEvent
            if (requestedLater) {
                val reviewRequestedEvent =
                    prTimelineEvents.find { it.eventType == ReviewRequestedEvent.TYPE && (it as ReviewRequestedEvent).requested_reviewer == reviewer } as ReviewRequestedEvent
                val openToCloseDuration = (
                    pullRequest.merged_at?.toInstant()
                        ?: approvedPrEvent.submitted_at.toInstant()
                    ) - reviewRequestedEvent.created_at.toInstant()
                val reviewTimeInWorkingHours = DateTimeDiffer.diffWorkingHours(
                    startInstant = reviewRequestedEvent.created_at.toInstant(),
                    endInstant = approvedPrEvent.submitted_at.toInstant(),
                    timeZoneId = userTimeZone.get(reviewer.login)
                )
                if (BuildConfig.DEBUG) {
                    println(
                        "  -- Reviewed in `$reviewTimeInWorkingHours` by `${reviewer.login}`. " +
                            "PR open->merged: $openToCloseDuration"
                    )
                }

                reviewTimesByUser[reviewer.login] = reviewTimeInWorkingHours
            } else {
                val openToCloseDuration = (
                    pullRequest.merged_at?.toInstant()
                        ?: approvedPrEvent.submitted_at.toInstant()
                    ) - prAvailableForReview
                val reviewTimeInWorkingHours = DateTimeDiffer.diffWorkingHours(
                    startInstant = prAvailableForReview,
                    endInstant = approvedPrEvent.submitted_at.toInstant(),
                    timeZoneId = userTimeZone.get(reviewer.login)
                )
                if (BuildConfig.DEBUG) {
                    println(
                        "  -- Reviewed in `$reviewTimeInWorkingHours` by `${reviewer.login}`. " +
                            "PR open->merged: $openToCloseDuration"
                    )
                }

                reviewTimesByUser[reviewer.login] = reviewTimeInWorkingHours
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
        prTimelineEvents: List<TimelineEvent>
    ): Set<User> {
        return prTimelineEvents.asSequence()
            .filter { it.eventType == ReviewRequestedEvent.TYPE }
            .map { it as ReviewRequestedEvent }
            .map { it.actor }.plus(
                prTimelineEvents.asSequence()
                    .filter { it.eventType == ReviewedEvent.TYPE }
                    .map { it as ReviewedEvent }
                    .map { it.user }
            ).toSet()
            .minus(prAuthor)
    }

    /**
     * Provides date-time when the PR was actually available for review
     * by considering PR creation time and ready for review event time.
     */
    private fun prAvailableForReviewTime(
        prCreatedOn: Instant,
        prTimelineEvents: List<TimelineEvent>
    ): Instant {
        val wasDraftPr: Boolean = prTimelineEvents.any { it.eventType == ReadyForReviewEvent.TYPE }

        if (wasDraftPr) {
            val readyForReview =
                prTimelineEvents.find { it.eventType == ReadyForReviewEvent.TYPE }!! as ReadyForReviewEvent
            return readyForReview.created_at.toInstant()
        }

        return prCreatedOn
    }
}
