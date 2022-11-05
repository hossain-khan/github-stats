package dev.hossain.githubstats.repository

import dev.hossain.githubstats.BuildConfig
import dev.hossain.githubstats.PrStats
import dev.hossain.githubstats.UserId
import dev.hossain.githubstats.UserPrComment
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
import kotlinx.datetime.Instant
import kotlinx.datetime.toInstant
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
        val pullRequest = githubApiService.pullRequest(repoOwner, repoId, prNumber)

        if (pullRequest.merged == null || pullRequest.merged == false) {
            // Skips PR stats generation if PR is not merged at all.
            return StatsResult.Failure(IllegalStateException("PR has not been merged, no reason to analyze PR stats."))
        }

        // API request to get all timeline events for the PR
        val prTimelineEvents = timelinesPager.getAllTimelineEvents(repoOwner, repoId, prNumber)
        // API request to get all PR review comments associated with diffs
        val prReviewComments = githubApiService.prReviewComments(repoOwner, repoId, prNumber)

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

        val commentsByUser: Map<UserId, UserPrComment> = commentsByUser(prTimelineEvents, prReviewComments)

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
     * Provides stats for users and total number of comments made in the PR
     * by analyzing all timeline events and PR review comments.
     *
     * > NOTE:
     * > Pull request review comments are comments on a portion of the unified diff made during a pull request review.
     * > Commit comments and issue comments are different from pull request review comments.
     *
     * Example snapshot of a map.
     * ```
     * swankjesse -> (issueComment=3, reviewComment=16)
     * jjshanks -> (issueComment=1, reviewComment=0)
     * yschimke -> (issueComment=9, reviewComment=21)
     * mjpitz -> (issueComment=10, reviewComment=16)
     * ```
     *
     * @return Map of `user-id -> total comments made`
     */
    private fun commentsByUser(
        prTimelineEvents: List<TimelineEvent>,
        prCodeReviewComments: List<CodeReviewComment>
    ): Map<UserId, UserPrComment> {
        val issueCommentsByUser = mutableMapOf<UserId, Int>()
        val reviewCommentsByUser = mutableMapOf<UserId, Int>()
        val reviewedEventByUser = mutableMapOf<UserId, Int>()

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

        prCodeReviewComments.forEach { codeReviewComment ->
            val commentsCount: Int? = reviewCommentsByUser[codeReviewComment.user.login]
            if (commentsCount != null) {
                reviewCommentsByUser[codeReviewComment.user.login] = commentsCount + 1
            } else {
                reviewCommentsByUser[codeReviewComment.user.login] = 1
            }
        }

        val prUserCommentsMap = (issueCommentsByUser.keys + reviewCommentsByUser.keys + reviewedEventByUser.keys)
            .associateWith { userId ->
                UserPrComment(
                    user = userId,
                    issueComment = issueCommentsByUser[userId] ?: 0,
                    codeReviewComment = reviewCommentsByUser[userId] ?: 0,
                    prReviewComment = reviewedEventByUser[userId] ?: 0
                )
            }

        return prUserCommentsMap
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
            val requestedLater = prTimelineEvents.filterTo(ReviewRequestedEvent::class)
                .any { it.requested_reviewer == reviewer }

            val prApprovedByReviewerEvent: ReviewedEvent = findPrApprovedEventByUser(reviewer, prTimelineEvents)

            if (requestedLater) {
                val reviewRequestedEvent = prTimelineEvents.filterTo(ReviewRequestedEvent::class)
                    .find { it.requested_reviewer == reviewer }!!
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
        return prTimelineEvents.filterTo(ReviewedEvent::class)
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
    private fun prReviewers(
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
