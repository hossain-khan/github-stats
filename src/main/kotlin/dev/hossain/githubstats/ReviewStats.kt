package dev.hossain.githubstats

import dev.hossain.githubstats.AppConstants.LOCAL_PROPERTIES_FILE
import dev.hossain.githubstats.model.PullRequest
import dev.hossain.githubstats.model.timeline.ReviewedEvent.ReviewState
import kotlinx.datetime.Instant
import kotlin.time.Duration

/**
 * Type alias for GitHub user login/id.
 */
typealias UserId = String

/**
 * Stats related to single PR.
 */
data class PrStats(
    /**
     * The PR information including PR number and URL.
     */
    val pullRequest: PullRequest,

    /**
     * A map containing `reviewer-id -> PR review time` in working hours (excludes weekends and after hours)
     */
    val prApprovalTime: Map<UserId, Duration>,

    /**
     * A map containing `reviewer-id -> PR initial response time` in working hours (excludes weekends and after hours)
     * The initial response time indicates the time it took for reviewer to first respond to PR
     * by either commenting, reviewing or approving the PR.
     */
    val initialResponseTime: Map<UserId, Duration>,

    /**
     * Map of `user-id -> total comments made` for the [pullRequest].
     */
    val comments: Map<UserId, UserPrComment>,

    /**
     * Date and time when the PR was ready for review for the specific author.
     */
    val prReadyOn: Instant,

    /**
     * Date and time when the PR was merged successfully.
     */
    val prMergedOn: Instant
)

/**
 * PR review stats for a specific reviewer ([reviewerUserId]).
 * This reviewer has reviewed the [pullRequest].
 */
data class ReviewStats constructor(
    /**
     * User who has reviewer the PR.
     */
    val reviewerUserId: UserId,
    /**
     * The PR information including PR number and URL.
     */
    val pullRequest: PullRequest,

    /**
     * PR review completion time in working hours (excludes weekends and after hours)
     */
    val reviewCompletion: Duration,

    /**
     * The initial response time indicates the time it took for reviewer to first respond to PR
     * by either commenting, reviewing or approving the PR.
     */
    val initialResponseTime: Duration,

    /**
     * Contains PR issue comment and review comment count by a specific user.
     */
    val prComments: UserPrComment,

    /**
     * Date and time when the PR was ready for review for the specific author.
     */
    val prReadyOn: Instant,

    /**
     * Date and time when the PR was merged successfully.
     */
    val prMergedOn: Instant
)

/**
 * Stats of list PRs that are reviewed by [reviewerId] user, which are authored by the [prAuthorId] user.
 * In other words, stats for reviewer [reviewerId], who has reviewed PRs for the [prAuthorId] user.
 *
 * The [stats] items contains PR review stats.
 *
 * @see PrAuthorStatsService
 */
class AuthorReviewStats(
    val repoId: String,
    val prAuthorId: UserId,
    val reviewerId: UserId,
    val average: Duration,
    val totalReviews: Int,
    val totalComments: Int,
    val stats: List<ReviewStats>
)

/**
 * Extension function that calculates average time to merge **all** PRs by specific PR author.
 * @see AuthorReviewStats
 */
fun List<AuthorReviewStats>.avgMergeTime(): Duration {
    val allPrReviewStatsForAuthor: List<ReviewStats> = this.map { it.stats }.flatten()
    return allPrReviewStatsForAuthor
        .map {
            val prOpenToMergeTime = it.prMergedOn - it.prReadyOn
            prOpenToMergeTime
        }
        .fold(Duration.ZERO, Duration::plus)
        .div(allPrReviewStatsForAuthor.size)
}

/**
 * Reviewer stats for **all** the PR reviews done in specific [repoId].
 * All PR review stats are available in [reviewedPrStats].
 *
 * @see PrReviewerStatsService
 */
data class ReviewerReviewStats(
    /**
     * The GitHub repository ID.
     */
    val repoId: String,
    /**
     * The reviewer's user id for whom all the stats are generated. Eg. [reviewedPrStats] and [reviewedForPrStats].
     */
    val reviewerId: UserId,
    /**
     * Average PR review duration.
     */
    val average: Duration,
    /**
     * Total PR reviews done by the [reviewerId] user withing date range defined in [LOCAL_PROPERTIES_FILE].
     */
    val totalReviews: Int,
    /**
     * Contains [ReviewStats] for all the PRs reviewed by [reviewerId].
     */
    val reviewedPrStats: List<ReviewStats>,
    /**
     * A hashmap for [Reviewed for UserID -> List of PR Reviewed and their Stats]
     * For example:
     * - john -> [PR#112 Stats, PR#931 Stats] (Meaning: The reviewer has reviewed 2 PRs created by `john`)
     * - kirk -> [PR#341 Stats, PR#611 Stats, PR#839 Stats]  (Meaning: The reviewer has reviewed 3 PRs created by `kirk`)
     */
    val reviewedForPrStats: Map<UserId, List<PrStats>>
)

/**
 * Contains PR issue comment and review comment count by a specific user.
 *
 * > Pull request review comments are comments on a portion of the unified diff made during a pull request review.
 * > Commit comments and issue comments are different from pull request review comments.
 */
data class UserPrComment(
    val user: UserId,
    /**
     * PR issue comments count that are directly commented on the PR.
     */
    val issueComment: Int,

    /**
     * Total PR review comments count.
     * Pull request review comments are comments on a portion of the unified diff made during a pull request review.
     */
    val codeReviewComment: Int,

    /**
     * Total PR review comment that either is [ReviewState.COMMENTED] or [ReviewState.CHANGE_REQUESTED].
     */
    val prReviewComment: Int
) {
    companion object {
        /**
         * Provides empty comments stats for specific [userId]/
         */
        fun noComments(userId: UserId) = UserPrComment(
            user = userId,
            issueComment = 0,
            codeReviewComment = 0,
            prReviewComment = 0
        )
    }

    val allComments: Int = issueComment + codeReviewComment + prReviewComment

    /**
     * Checks if stats is empty, then it's likely not worth showing.
     */
    fun isEmpty(): Boolean = issueComment == 0 && codeReviewComment == 0 && prReviewComment == 0

    override fun toString(): String {
        return "$user made $issueComment PR comment and $codeReviewComment review comment " +
            "and has reviewed PR $prReviewComment times. Total: $allComments comments."
    }
}
