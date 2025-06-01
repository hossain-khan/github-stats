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
import dev.hossain.githubstats.util.ErrorInfo
import dev.hossain.time.DateTimeDiffer
import dev.hossain.time.UserTimeZone
import dev.hossain.time.format
import kotlinx.datetime.Instant
import kotlin.time.Duration

/**
 * Creates PR stats using combination of data from the PR using [githubApiService].
 */
class PullRequestStatsRepoImpl(
    private val githubApiService: GithubApiService,
    private val timelinesPager: TimelineEventsPagerService,
    private val userTimeZone: UserTimeZone,
) : PullRequestStatsRepo {
    /**
     * Provides Pull Request stats [PrStats] for given [prNumber].
     */
    override suspend fun stats(
        repoOwner: String,
        repoId: String,
        prNumber: Int,
        botUserIds: List<String>,
    ): StatsResult {
        val pullRequest: PullRequest = githubApiService.pullRequest(repoOwner, repoId, prNumber)

        // Validate PR for stats generation
        validatePrForStats(pullRequest, botUserIds)?.let { return it }

        // Get all timeline events and code review comments for the PR
        val prTimelineEvents = timelinesPager.getAllTimelineEvents(repoOwner, repoId, prNumber)
        val prCodeReviewComments = githubApiService.prSourceCodeReviewComments(repoOwner, repoId, prNumber)

        Log.i("\n- Getting PR#$prNumber info. Analyzing ${prTimelineEvents.size} events from the PR. (URL: ${pullRequest.html_url})")

        val prAvailableForReviewOn = prAvailableForReviewTime(pullRequest.prCreatedOn, prTimelineEvents)

        // Get the set of human reviewers for the PR
        val prReviewers = getPrReviewers(pullRequest, prTimelineEvents, botUserIds)
            ?: return StatsResult.Failure(
                ErrorInfo(
                    errorMessage = "No human reviewers found for PR#${pullRequest.number}. ",
                    exception = IllegalStateException("No human reviewers found for PR#${pullRequest.number}."),
                ),
            )

        // Calculate review metrics (initial response time and approval time)
        val (prInitialResponseTimeMap, prReviewCompletionMap) =
            calculateReviewMetrics(
                pullRequest = pullRequest,
                prAvailableForReviewOn = prAvailableForReviewOn,
                prReviewers = prReviewers,
                prTimelineEvents = prTimelineEvents,
            )

        // Calculate comments by user
        val commentsByUser: Map<UserId, UserPrComment> =
            prCommentsCountByUser(
                prTimelineEvents = prTimelineEvents,
                prCodeReviewComments = prCodeReviewComments,
            )

        return StatsResult.Success(
            PrStats(
                pullRequest = pullRequest,
                prApprovalTime = prReviewCompletionMap,
                initialResponseTime = prInitialResponseTimeMap,
                comments = commentsByUser,
                prReadyOn = prAvailableForReviewOn,
                prMergedOn = pullRequest.prMergedOn!!,
            ),
        )
    }

    // region: Private helper functions for stats calculation
    // =======================================================

    /**
     * Validates if the PR should be processed for stats.
     * Returns [StatsResult.Failure] if validation fails, `null` otherwise.
     */
    private fun validatePrForStats(
        pullRequest: PullRequest,
        botUserIds: List<String>,
    ): StatsResult? {
        if (!pullRequest.isMerged) {
            Log.v("The PR#${pullRequest.number} is not merged. Skipping PR stat analysis.")
            return StatsResult.Failure(
                ErrorInfo(
                    errorMessage = "PR#${pullRequest.number} is not merged, no reason to analyze PR stats.",
                    exception = IllegalStateException("PR#${pullRequest.number} is not merged."),
                ),
            )
        }

        if (pullRequest.user.login in botUserIds) {
            Log.i("The PR#${pullRequest.number} is created by bot user '${pullRequest.user.login}'. Skipping PR stat analysis.")
            return StatsResult.Failure(
                ErrorInfo(
                    errorMessage =
                        "PR#${pullRequest.number} is created by bot user '${pullRequest.user.login}', " +
                            "no reason to analyze PR stats.",
                    exception = IllegalStateException("PR#${pullRequest.number} is created by bot user '${pullRequest.user.login}'."),
                ),
            )
        }
        return null
    }

    /**
     * Gets the set of human reviewers for the PR.
     * Returns `null` if no human reviewers are found (logging the original reviewers).
     */
    private fun getPrReviewers(
        pullRequest: PullRequest,
        prTimelineEvents: List<TimelineEvent>,
        botUserIds: List<String>,
    ): Set<User>? {
        val allReviewerUsers = extractPrReviewersFromEvents(pullRequest.user, prTimelineEvents)
        val humanReviewers = allReviewerUsers.filter { it.login !in botUserIds }.toSet()

        if (humanReviewers.isEmpty()) {
            Log.w(
                "No human reviewers found for PR#${pullRequest.number}. " +
                    "Original reviewers: ${allReviewerUsers.map { it.login }}. Skipping PR stat analysis.",
            )
            return null
        }
        return humanReviewers
    }

    /**
     * Extracts all unique users who were requested to review or have reviewed the PR, excluding the PR author.
     */
    private fun extractPrReviewersFromEvents(
        prAuthor: User,
        prTimelineEvents: List<TimelineEvent>,
    ): Set<User> =
        prTimelineEvents
            .filterTo(ReviewRequestedEvent::class)
            .map { it.actor } // User who requested the review
            .plus(
                prTimelineEvents.filterTo(ReviewedEvent::class).map { it.user }, // User who submitted the review
            )
            .plus(
                prTimelineEvents.filterTo(ReviewRequestedEvent::class).mapNotNull { it.requested_reviewer }, // The user/team requested
            )
            .toSet()
            .minus(prAuthor) // Exclude PR author from reviewers list

    /**
     * Calculates both initial response time and PR approval time for each reviewer.
     * Iterates over the reviewers once to build both maps.
     */
    private fun calculateReviewMetrics(
        pullRequest: PullRequest,
        prAvailableForReviewOn: Instant,
        prReviewers: Set<User>,
        prTimelineEvents: List<TimelineEvent>,
    ): Pair<Map<UserId, Duration>, Map<UserId, Duration>> {
        val initialResponseTimeMap = mutableMapOf<UserId, Duration>()
        val reviewCompletionTimeMap = mutableMapOf<UserId, Duration>()

        prReviewers.forEach { reviewer ->
            val prReviewerUserId = reviewer.login
            val prReadyForReviewOnByUser = evaluatePrReadyForReviewByUser(reviewer, prAvailableForReviewOn, prTimelineEvents)

            // Calculate Initial Response Time
            firstReviewedEvent(prTimelineEvents, reviewer, prReadyForReviewOnByUser)?.let { firstEvent ->
                val initialResponseDuration =
                    DateTimeDiffer.diffWorkingHours(
                        startInstant = prReadyForReviewOnByUser,
                        endInstant = Instant.parse(firstEvent.submitted_at),
                        timeZoneId = userTimeZone.get(prReviewerUserId),
                    )
                initialResponseTimeMap[prReviewerUserId] = initialResponseDuration
                Log.d(
                    "  -- First Responded[${firstEvent.state.name.lowercase()}] in `$initialResponseDuration` by `$prReviewerUserId`.",
                )
                Log.v(
                    "     -- 🔍👀 Initial response event: $firstEvent. PR available on ${prAvailableForReviewOn.format()}," +
                        "ready for reviewer on ${prReadyForReviewOnByUser.format()} " +
                        "and event on ${Instant.parse(firstEvent.submitted_at).format()}",
                )
            }

            // Calculate Review Completion Time (Approval Time)
            if (isApprovedByReviewer(reviewer, prTimelineEvents)) {
                val approvedEvent = findPrApprovedEventByUser(reviewer, prTimelineEvents)
                val approvalDuration =
                    DateTimeDiffer.diffWorkingHours(
                        startInstant = prReadyForReviewOnByUser,
                        endInstant = Instant.parse(approvedEvent.submitted_at),
                        timeZoneId = userTimeZone.get(prReviewerUserId),
                    )
                reviewCompletionTimeMap[prReviewerUserId] = approvalDuration

                // Log the original open to merge duration for context
                val openToCloseDuration = pullRequest.prMergedOn!! - prAvailableForReviewOn
                Log.i(
                    "  -- Reviewed and ✔approved in `$approvalDuration` by `$prReviewerUserId`. " +
                        "PR open->merged: $openToCloseDuration",
                )
            }
        }

        return Pair(initialResponseTimeMap, reviewCompletionTimeMap)
    }

    /**
     * Provides stats for users and total number of comments made in the PR
     * by analyzing all timeline events and PR review comments.
     */
    private fun prCommentsCountByUser(
        prTimelineEvents: List<TimelineEvent>,
        prCodeReviewComments: List<CodeReviewComment>,
    ): Map<UserId, UserPrComment> {
        val issueCommentsByUser = mutableMapOf<UserId, Int>()
        val codeReviewCommentsByUser = mutableMapOf<UserId, Int>()
        val reviewedEventByUser = mutableMapOf<UserId, Int>()

        prTimelineEvents
            .filterTo(CommentedEvent::class)
            .forEach { commentedEvent ->
                issueCommentsByUser[commentedEvent.user.login] =
                    (issueCommentsByUser[commentedEvent.user.login] ?: 0) + 1
            }

        prTimelineEvents
            .filterTo(ReviewedEvent::class)
            .filter { it.state == ReviewState.COMMENTED || it.state == ReviewState.CHANGE_REQUESTED }
            .forEach { reviewedEvent ->
                reviewedEventByUser[reviewedEvent.user.login] =
                    (reviewedEventByUser[reviewedEvent.user.login] ?: 0) + 1
            }

        prCodeReviewComments.forEach { codeReviewComment ->
            codeReviewCommentsByUser[codeReviewComment.user.login] =
                (codeReviewCommentsByUser[codeReviewComment.user.login] ?: 0) + 1
        }

        val allCommenters = issueCommentsByUser.keys + codeReviewCommentsByUser.keys + reviewedEventByUser.keys

        return allCommenters
            .associateWith { userId ->
                UserPrComment(
                    user = userId,
                    issueComment = issueCommentsByUser[userId] ?: 0,
                    codeReviewComment = codeReviewCommentsByUser[userId] ?: 0,
                    prReviewSubmissionComment = reviewedEventByUser[userId] ?: 0,
                )
            }
    }

    /**
     * Evaluates actual time when PR was available for user to review by considering
     * if review was requested later from the specified [reviewer].
     * Or, if user self reviewed PR without being added, then the original time will be used.
     */
    private fun evaluatePrReadyForReviewByUser(
        reviewer: User,
        prAvailableForReviewOn: Instant,
        prTimelineEvents: List<TimelineEvent>,
    ): Instant {
        val reviewRequestedEvent: ReviewRequestedEvent? =
            prTimelineEvents
                .filterTo(ReviewRequestedEvent::class)
                .find { it.requested_reviewer == reviewer }

        val reviewedByUserEvent: ReviewedEvent? =
            prTimelineEvents
                .filterTo(ReviewedEvent::class)
                .find { it.user == reviewer }

        // If user was requested to review AFTER they already submitted a review,
        // consider the PR available from the initial PR available time for that user.
        if (reviewRequestedEvent != null && reviewedByUserEvent != null) {
            if (Instant.parse(reviewRequestedEvent.created_at) > Instant.parse(reviewedByUserEvent.submitted_at)) {
                return prAvailableForReviewOn
            }
        }

        // Determines PR readiness time for reviewer: if review was requested later for the user, use that time.
        return reviewRequestedEvent?.created_at?.let { Instant.parse(it) } ?: prAvailableForReviewOn
    }

    /**
     * Checks if the PR was approved by the [reviewer] by analyzing the [prTimelineEvents].
     */
    private fun isApprovedByReviewer(
        reviewer: User,
        prTimelineEvents: List<TimelineEvent>,
    ): Boolean =
        prTimelineEvents
            .filterTo(ReviewedEvent::class)
            .any { it.user == reviewer && it.state == ReviewState.APPROVED }

    /**
     * Finds the first reviewed event with [ReviewState.APPROVED] state
     * for the user to know when user has finished reviewing the PR.
     * NOTE: The [isApprovedByReviewer] must be checked before using this.
     */
    private fun findPrApprovedEventByUser(
        reviewer: User,
        prTimelineEvents: List<TimelineEvent>,
    ): ReviewedEvent =
        prTimelineEvents
            .filterTo(ReviewedEvent::class)
            .find { it.user == reviewer && it.state == ReviewState.APPROVED }!!

    /**
     * Provides date-time when the PR was actually available for review
     * by considering PR creation time and ready for review event time.
     */
    private fun prAvailableForReviewTime(
        prCreatedOn: Instant,
        prTimelineEvents: List<TimelineEvent>,
    ): Instant {
        val readyForReviewEvent = prTimelineEvents.filterIsInstance<ReadyForReviewEvent>().firstOrNull()
        return readyForReviewEvent?.created_at?.let { Instant.parse(it) } ?: prCreatedOn
    }

    /**
     * Finds the first reviewed event by the [reviewer] after the PR was ready for their review.
     * This includes any type of review action (approved, commented, requested changes).
     */
    private fun firstReviewedEvent(
        prTimelineEvents: List<TimelineEvent>,
        reviewer: User,
        prReadyForReviewOn: Instant,
    ): ReviewedEvent? =
        prTimelineEvents
            .filterTo(ReviewedEvent::class)
            .filter {
                it.user == reviewer &&
                    Instant.parse(it.submitted_at) >= prReadyForReviewOn &&
                    (it.state == ReviewState.APPROVED || it.state == ReviewState.CHANGE_REQUESTED || it.state == ReviewState.COMMENTED)
            }
            .minByOrNull { Instant.parse(it.submitted_at) } // Get the earliest review event
}
// endregion: Private helper functions for stats calculation
// =======================================================
