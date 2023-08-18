package dev.hossain.githubstats.repository

import dev.hossain.githubstats.PrStats
import dev.hossain.githubstats.UserId
import dev.hossain.githubstats.UserPrComment
import dev.hossain.githubstats.logging.Log
import dev.hossain.githubstats.model.CodeReviewComment
import dev.hossain.githubstats.model.PullRequest
import dev.hossain.githubstats.model.User
import dev.hossain.githubstats.model.timeline.CommentedEvent
import dev.hossain.githubstats.model.timeline.ReadyForReviewEvent
import dev.hossain.githubstats.model.timeline.ReviewRequestedEvent
import dev.hossain.githubstats.model.timeline.ReviewedEvent
import dev.hossain.githubstats.model.timeline.ReviewedEvent.ReviewState
import dev.hossain.githubstats.model.timeline.TimelineEvent
import dev.hossain.githubstats.model.timeline.filterTo
import dev.hossain.githubstats.repository.PullRequestStatsRepo.StatsResult
import dev.hossain.githubstats.service.GithubApiService
import dev.hossain.githubstats.service.TimelineEventsPagerService
import dev.hossain.time.DateTimeDiffer
import dev.hossain.time.UserTimeZone
import dev.hossain.time.format
import kotlinx.datetime.Instant
import kotlinx.datetime.toInstant
import org.jetbrains.annotations.TestOnly
import kotlin.time.Duration

/**
 * Creates PR stats using combination of data from the PR using [githubApiService].
 */
class PullRequestStatsRepoImpl(
    private val githubApiService: GithubApiService,
    private val timelinesPager: TimelineEventsPagerService,
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
        val pullRequest: PullRequest = githubApiService.pullRequest(repoOwner, repoId, prNumber)

        if (!pullRequest.isMerged) {
            // Skips PR stats generation if PR is not merged at all.
            Log.v("The PR#${pullRequest.number} is not merged. Skipping PR stat analysis.")
            return StatsResult.Failure(IllegalStateException("PR has not been merged, no reason to analyze PR stats."))
        }

        // API request to get all timeline events for the PR
        val prTimelineEvents = timelinesPager.getAllTimelineEvents(repoOwner, repoId, prNumber)
        // API request to get all PR source code review comments associated with diffs
        val prCodeReviewComments = githubApiService.prSourceCodeReviewComments(repoOwner, repoId, prNumber)

        Log.i("\n- Getting PR#$prNumber info. Analyzing ${prTimelineEvents.size} events from the PR. (URL: ${pullRequest.html_url})")

        val prAvailableForReviewOn: Instant = prAvailableForReviewTime(pullRequest.prCreatedOn, prTimelineEvents)

        // List of users who has been requested as reviewer or reviewed the PR
        val prReviewers: Set<User> = prReviewers(pullRequest.user, prTimelineEvents)

        // Builds a map of [Reviewer User -> Initial response time by either commenting, reviewing or approving PR]
        val prInitialResponseTimeMap: Map<UserId, Duration> = prInitialResponseTimeByUser(
            prAvailableForReviewOn = prAvailableForReviewOn,
            prReviewers = prReviewers,
            prTimelineEvents = prTimelineEvents
        )

        // Builds a map of [Reviewer User -> Review Time during Working Hours]
        val prReviewCompletionMap: Map<String, Duration> = prReviewTimeByUser(
            pullRequest = pullRequest,
            prAvailableForReviewOn = prAvailableForReviewOn,
            prReviewers = prReviewers,
            prTimelineEvents = prTimelineEvents
        )

        val commentsByUser: Map<UserId, UserPrComment> = prCommentsCountByUser(
            prTimelineEvents = prTimelineEvents,
            prCodeReviewComments = prCodeReviewComments
        )

        return StatsResult.Success(
            PrStats(
                pullRequest = pullRequest,
                prApprovalTime = prReviewCompletionMap,
                initialResponseTime = prInitialResponseTimeMap,
                comments = commentsByUser,
                prReadyOn = prAvailableForReviewOn,
                prMergedOn = pullRequest.prMergedOn!!
            )
        )
    }

    /**
     * Provides stats for users and total number of comments made in the PR
     * by analyzing all timeline events and PR review comments.
     *
     * > NOTE:
     * > Pull request review comments are comments on a portion of the unified diff made during a pull request review.
     * > Commit comments and issue comments are different from pull request review comments.
     *
     * Example snapshot of a map.
     * ```
     * swankjesse -> (issueComment=3, codeReviewComment=3, prReviewComment=7)
     * jjshanks -> (issueComment=1, codeReviewComment=0, prReviewComment=0)
     * yschimke -> (issueComment=9, codeReviewComment=21, prReviewComment=2)
     * mjpitz -> (issueComment=10, codeReviewComment=7, prReviewComment=1)
     * ```
     *
     * @return Map of `user-id -> count of various type of comments made`. See [UserPrComment].
     */
    private fun prCommentsCountByUser(
        prTimelineEvents: List<TimelineEvent>,
        prCodeReviewComments: List<CodeReviewComment>
    ): Map<UserId, UserPrComment> {
        val issueCommentsByUser = mutableMapOf<UserId, Int>()
        val codeReviewCommentsByUser = mutableMapOf<UserId, Int>()
        val reviewedEventByUser = mutableMapOf<UserId, Int>()

        // Collect all comments made on the PR issue itself, without referencing any line-of-code.
        prTimelineEvents.filterTo(CommentedEvent::class)
            .forEach { commentedEvent ->
                val commentsCount: Int? = issueCommentsByUser[commentedEvent.user.login]
                if (commentsCount != null) {
                    issueCommentsByUser[commentedEvent.user.login] = commentsCount + 1
                } else {
                    issueCommentsByUser[commentedEvent.user.login] = 1
                }
            }

        // Collect all the reviewed comments that are comments or changes requested
        prTimelineEvents.filterTo(ReviewedEvent::class)
            .filter { it.state == ReviewState.COMMENTED || it.state == ReviewState.CHANGE_REQUESTED }
            .forEach { reviewedEvent ->
                val reviewedCount: Int? = reviewedEventByUser[reviewedEvent.user.login]
                if (reviewedCount != null) {
                    reviewedEventByUser[reviewedEvent.user.login] = reviewedCount + 1
                } else {
                    reviewedEventByUser[reviewedEvent.user.login] = 1
                }
            }

        // Collect all code review comment made on line-of-code from diff
        prCodeReviewComments.forEach { codeReviewComment ->
            val commentsCount: Int? = codeReviewCommentsByUser[codeReviewComment.user.login]
            if (commentsCount != null) {
                codeReviewCommentsByUser[codeReviewComment.user.login] = commentsCount + 1
            } else {
                codeReviewCommentsByUser[codeReviewComment.user.login] = 1
            }
        }

        val prUserCommentsMap = (issueCommentsByUser.keys + codeReviewCommentsByUser.keys + reviewedEventByUser.keys)
            .associateWith { userId ->
                UserPrComment(
                    user = userId,
                    issueComment = issueCommentsByUser[userId] ?: 0,
                    codeReviewComment = codeReviewCommentsByUser[userId] ?: 0,
                    prReviewSubmissionComment = reviewedEventByUser[userId] ?: 0
                )
            }

        return prUserCommentsMap
    }

    /**
     * Provides initial response time by user for the pull request.
     * The initial response time indicates the time it took for reviewer to first respond to PR
     * by either commenting on the changes, reviewing and asking for change or approving the PR.
     */
    private fun prInitialResponseTimeByUser(
        prAvailableForReviewOn: Instant,
        prReviewers: Set<User>,
        prTimelineEvents: List<TimelineEvent>
    ): Map<UserId, Duration> {
        val initialResponseTime = mutableMapOf<UserId, Duration>()

        prReviewers.forEach { reviewer ->
            val firstReviewedEvent: ReviewedEvent = prTimelineEvents.filterTo(ReviewedEvent::class)
                // Finds first event (TODO: Check if the result is sorted, if not sort it)
                .find { reviewedEvent ->
                    reviewedEvent.user == reviewer &&
                        listOf(
                            ReviewState.APPROVED,
                            ReviewState.CHANGE_REQUESTED,
                            ReviewState.COMMENTED
                        ).any { it == reviewedEvent.state }
                } ?: return@forEach

            val prReviewerUserId = reviewer.login
            val prReadyForReviewOn = evaluatePrReadyForReviewByUser(reviewer, prAvailableForReviewOn, prTimelineEvents)

            // Calculates the PR review time in working hour by the reviewer on their time-zone (if configured)
            val reviewTimeInWorkingHours = DateTimeDiffer.diffWorkingHours(
                startInstant = prReadyForReviewOn,
                endInstant = firstReviewedEvent.submitted_at.toInstant(),
                timeZoneId = userTimeZone.get(prReviewerUserId)
            )
            Log.d("  -- First Responded[${firstReviewedEvent.state.name.lowercase()}] in `$reviewTimeInWorkingHours` by `$prReviewerUserId`.")
            Log.v(
                "     -- 🔍👀 Initial response event: $firstReviewedEvent. PR available on ${prAvailableForReviewOn.format()}," +
                    "ready for reviewer on ${prReadyForReviewOn.format()} " +
                    "and event on ${firstReviewedEvent.submitted_at.toInstant().format()}"
            )

            initialResponseTime[prReviewerUserId] = reviewTimeInWorkingHours
        }

        return initialResponseTime
    }

    /**
     * Provides the time required to approve the PR.
     *
     * NOTE: Future improvement, should provide both metrics:
     * - Time to first review
     * - Turn around time to approve
     */
    private fun prReviewTimeByUser(
        pullRequest: PullRequest,
        prAvailableForReviewOn: Instant,
        prReviewers: Set<User>,
        prTimelineEvents: List<TimelineEvent>
    ): Map<UserId, Duration> {
        val reviewTimesByUser = mutableMapOf<UserId, Duration>()

        prReviewers.forEach { reviewer ->
            // Find out if user has approved the PR, if not, do not include in stats
            if (!isApprovedByReviewer(reviewer, prTimelineEvents)) {
                // This ensures that `ReviewedEvent` by the reviewer exists in the timeline used later in the loop
                return@forEach
            }

            val prReviewerUserId = reviewer.login
            val prReadyForReviewOn = evaluatePrReadyForReviewByUser(reviewer, prAvailableForReviewOn, prTimelineEvents)

            val prApprovedByReviewerEvent: ReviewedEvent = findPrApprovedEventByUser(reviewer, prTimelineEvents)

            // Calculates PR open to merge duration (without considering any working hours)
            val openToCloseDuration = pullRequest.prMergedOn!! - prAvailableForReviewOn

            // Calculates the PR review time in working hour by the reviewer on their time-zone (if configured)
            val reviewTimeInWorkingHours = DateTimeDiffer.diffWorkingHours(
                startInstant = prReadyForReviewOn,
                endInstant = prApprovedByReviewerEvent.submitted_at.toInstant(),
                timeZoneId = userTimeZone.get(prReviewerUserId)
            )
            Log.i(
                "  -- Reviewed and ✔approved in `$reviewTimeInWorkingHours` by `$prReviewerUserId`. " +
                    "PR open->merged: $openToCloseDuration"
            )

            reviewTimesByUser[prReviewerUserId] = reviewTimeInWorkingHours
        }

        return reviewTimesByUser
    }

    /**
     * Evaluates actual time when PR was available for use to review by considering
     * if review was requested later from the specified [reviewer].
     * Or, if user self reviewed PR without being added, then the original time will be used.
     */
    private fun evaluatePrReadyForReviewByUser(
        reviewer: User,
        prAvailableForReviewOn: Instant,
        prTimelineEvents: List<TimelineEvent>
    ): Instant {
        // Find out if user has been requested to review later
        val reviewRequestedEvent: ReviewRequestedEvent? = prTimelineEvents.filterTo(ReviewRequestedEvent::class)
            .find { it.requested_reviewer == reviewer }

        val reviewedByUserEvent: ReviewedEvent? = prTimelineEvents.filterTo(ReviewedEvent::class)
            .find { it.user == reviewer }

        if (reviewRequestedEvent != null && reviewedByUserEvent != null) {
            // This is an edge case where user as reviewed PR and then was requested to review later
            if (reviewRequestedEvent.created_at.toInstant() > reviewedByUserEvent.submitted_at.toInstant()) {
                return prAvailableForReviewOn
            }
        }

        // Determines PR readiness time for reviewer if review was requested later for the user
        return reviewRequestedEvent?.created_at?.toInstant() ?: prAvailableForReviewOn
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
        return prTimelineEvents.filterTo(ReviewedEvent::class)
            .any { it.user == reviewer && it.state == ReviewState.APPROVED }
    }

    /**
     * Finds the first reviewed event with [ReviewState.APPROVED] state
     * for the user to know when user has finished reviewing the PR.
     *
     *
     * _NOTE: The [isApprovedByReviewer] must be checked before using this, else [NullPointerException] may be thrown._
     *
     * @param reviewer The user who reviewed the PR.
     * @param prTimelineEvents All the timeline events for the opened PR.
     * @see isApprovedByReviewer
     */
    private fun findPrApprovedEventByUser(
        reviewer: User,
        prTimelineEvents: List<TimelineEvent>
    ): ReviewedEvent {
        return prTimelineEvents.filterTo(ReviewedEvent::class)
            .find { it.user == reviewer && it.state == ReviewState.APPROVED }!!
    }

    /**
     * Extracts all the PR reviewers who reviewed (approved or reviewed)
     * or has been requested to review.
     *
     * @param prAuthor The user who created the PR
     * @param prTimelineEvents All the timeline events for the opened PR.
     */
    @TestOnly
    internal fun prReviewers(
        prAuthor: User,
        prTimelineEvents: List<TimelineEvent>
    ): Set<User> {
        return prTimelineEvents
            .filterTo(ReviewRequestedEvent::class)
            .map { it.actor }
            .plus(
                prTimelineEvents.filterTo(ReviewedEvent::class).map { it.user }
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
