package dev.hossain.githubstats.repository

import dev.hossain.githubstats.BuildConfig
import dev.hossain.githubstats.PrStats
import dev.hossain.githubstats.UserId
import dev.hossain.githubstats.model.PullRequest
import dev.hossain.githubstats.model.User
import dev.hossain.githubstats.model.timeline.CommentedEvent
import dev.hossain.githubstats.model.timeline.ReadyForReviewEvent
import dev.hossain.githubstats.model.timeline.ReviewRequestedEvent
import dev.hossain.githubstats.model.timeline.ReviewedEvent
import dev.hossain.githubstats.model.timeline.ReviewedEvent.ReviewState
import dev.hossain.githubstats.model.timeline.TimelineEvent
import dev.hossain.githubstats.repository.PullRequestStatsRepo.StatsResult
import dev.hossain.githubstats.service.GithubApiService
import dev.hossain.time.DateTimeDiffer
import dev.hossain.time.UserTimeZone
import kotlinx.datetime.Instant
import kotlinx.datetime.toInstant
import kotlin.time.Duration

/**
 * Creates PR stats using combination of data from the PR using [githubApiService].
 */
class PullRequestStatsRepoImpl(
    private val githubApiService: GithubApiService,
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
        // API request to get PR information
        val pullRequest = githubApiService.pullRequest(repoOwner, repoId, prNumber)
        // API request to get all timeline events for the PR
        val prTimelineEvents = githubApiService.timelineEvents(repoOwner, repoId, prNumber)

        if (pullRequest.merged == null || pullRequest.merged == false) {
            // Skips PR stats generation if PR is not merged at all.
            return StatsResult.Failure(IllegalStateException("PR has not been merged, no reason to analyze PR stats."))
        }

        if (BuildConfig.DEBUG) {
            println("\n- Getting PR#$prNumber info. Analyzing ${prTimelineEvents.size} events from the PR. (URL: ${pullRequest.html_url})")
        }

        val prCreatedOn: Instant = pullRequest.created_at.toInstant()
        val prAvailableForReviewOn: Instant = prAvailableForReviewTime(prCreatedOn, prTimelineEvents)

        // List of users who has been requested as reviewer or reviewed the PR
        val prReviewers: Set<User> = prReviewers(pullRequest.user, prTimelineEvents)

        // Builds a map of [Reviewer User -> Review Time during Working Hours]
        val prReviewCompletionMap: Map<String, Duration> = reviewTimeByUser(
            pullRequest = pullRequest,
            prAvailableForReview = prAvailableForReviewOn,
            prReviewers = prReviewers,
            prTimelineEvents = prTimelineEvents
        )

        val commentsByUser: Map<UserId, Int> = commentsByUser(prTimelineEvents)

        return StatsResult.Success(
            PrStats(
                pullRequest = pullRequest,
                reviewTime = prReviewCompletionMap,
                comments = commentsByUser,
                prReadyOn = prAvailableForReviewOn,
                prMergedOn = pullRequest.merged_at!!.toInstant()
            )
        )
    }

    /**
     * Provides stats for users and total number of comments made in the PR by analyzing all timeline events.
     *
     * > NOTE: These are the PR issue comments, not PR review comments on specific commits or change-set.
     * > Pull request review comments are comments on a portion of the unified diff made during a pull request review.
     * > Commit comments and issue comments are different from pull request review comments.
     *
     * Example snapshot of a map.
     * ```
     * {swankjesse=3, jjshanks=1, yschimke=9, mjpitz=10, JakeWharton=1}
     * ```
     *
     * @return Map of `user-id -> total comments made`
     */
    private fun commentsByUser(prTimelineEvents: List<TimelineEvent>): Map<UserId, Int> {
        val commentsByUser = mutableMapOf<UserId, Int>()

        prTimelineEvents
            .filter { it.eventType == CommentedEvent.TYPE }
            .map { it as CommentedEvent }
            .forEach { commentedEvent ->
                if (commentsByUser.containsKey(commentedEvent.user.login)) {
                    commentsByUser[commentedEvent.user.login] = commentsByUser[commentedEvent.user.login]!! + 1
                } else {
                    commentsByUser[commentedEvent.user.login] = 1
                }
            }

        return commentsByUser
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
    ): Map<UserId, Duration> {
        val reviewTimesByUser = mutableMapOf<UserId, Duration>()

        prReviewers.forEach { reviewer ->
            // Find out if user has approved the PR, if not, do not include in stats
            if (!isApprovedByReviewer(reviewer, prTimelineEvents)) {
                return@forEach
            }

            // Find out if user has been requested to review later
            val requestedLater = prTimelineEvents.asSequence().filter { it.eventType == ReviewRequestedEvent.TYPE }
                .map { it as ReviewRequestedEvent }.any { it.requested_reviewer == reviewer }

            val prApprovedByReviewerEvent: ReviewedEvent = findPrApprovedEventByUser(reviewer, prTimelineEvents)

            if (requestedLater) {
                val reviewRequestedEvent =
                    prTimelineEvents.find { it.eventType == ReviewRequestedEvent.TYPE && (it as ReviewRequestedEvent).requested_reviewer == reviewer } as ReviewRequestedEvent
                val openToCloseDuration = (
                    pullRequest.merged_at?.toInstant()
                        ?: prApprovedByReviewerEvent.submitted_at.toInstant()
                    ) - reviewRequestedEvent.created_at.toInstant()
                val reviewTimeInWorkingHours = DateTimeDiffer.diffWorkingHours(
                    startInstant = reviewRequestedEvent.created_at.toInstant(),
                    endInstant = prApprovedByReviewerEvent.submitted_at.toInstant(),
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
                        ?: prApprovedByReviewerEvent.submitted_at.toInstant()
                    ) - prAvailableForReview
                val reviewTimeInWorkingHours = DateTimeDiffer.diffWorkingHours(
                    startInstant = prAvailableForReview,
                    endInstant = prApprovedByReviewerEvent.submitted_at.toInstant(),
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
     * Checks if the PR was approved by the [reviewer] by analyzing the [prTimelineEvents].
     *
     * @param reviewer The user who reviewed the PR.
     * @param prTimelineEvents All the timeline events for the opened PR.
     */
    private fun isApprovedByReviewer(
        reviewer: User,
        prTimelineEvents: List<TimelineEvent>
    ): Boolean {
        return prTimelineEvents.asSequence()
            .filter { it.eventType == ReviewedEvent.TYPE }
            .map { it as ReviewedEvent }
            .any { it.user == reviewer && it.state == ReviewState.APPROVED }
    }

    /**
     * Finds the first reviewed event with [ReviewState.APPROVED] state
     * for the user to know when user has finished reviewing the PR.
     *
     * @param reviewer The user who reviewed the PR.
     * @param prTimelineEvents All the timeline events for the opened PR.
     */
    private fun findPrApprovedEventByUser(
        reviewer: User,
        prTimelineEvents: List<TimelineEvent>
    ): ReviewedEvent {
        return prTimelineEvents.find {
            it.eventType == ReviewedEvent.TYPE &&
                (it as ReviewedEvent).user == reviewer &&
                it.state == ReviewState.APPROVED
        } as ReviewedEvent
    }

    /**
     * Extracts all the PR reviewers who reviewed (approved or reviewed)
     * or has been requested to review.
     *
     * @param prAuthor The user who created the PR
     * @param prTimelineEvents All the timeline events for the opened PR.
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
