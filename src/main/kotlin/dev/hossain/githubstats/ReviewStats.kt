package dev.hossain.githubstats

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
    val reviewTime: Map<UserId, Duration>,

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
 * PR review stats for a specific author.
 */
data class ReviewStats(
    /**
     * The PR information including PR number and URL.
     */
    val pullRequest: PullRequest,

    /**
     * PR review completion time in working hours (excludes weekends and after hours)
     */
    val reviewCompletion: Duration,

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
 * Reviewer stats for all the reviews done in specific [repoId].
 */
data class ReviewerReviewStats(
    val repoId: String,
    val reviewerId: UserId,
    val average: Duration,
    val totalReviews: Int,
    val reviewedPrStats: List<ReviewStats>,
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
        fun empty(userId: UserId) = UserPrComment(userId, 0, 0, 0)
    }

    val allComments: Int = issueComment + codeReviewComment + prReviewComment

    /**
     * Checks if stats is empty, then it's likely not worth showing.
     */
    fun empty(): Boolean = issueComment == 0 && codeReviewComment == 0 && prReviewComment == 0

    override fun toString(): String {
        return "$user made $issueComment PR comment and $codeReviewComment review comment " +
            "and has reviewed PR $prReviewComment times. Total: $allComments comments."
    }
}
