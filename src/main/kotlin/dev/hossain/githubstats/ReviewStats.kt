package dev.hossain.githubstats

import dev.hossain.githubstats.model.PullRequest
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
     * Date and time when the PR was ready for review for the specific author.
     */
    val prReadyOn: Instant,
    /**
     * Date and time when the PR was merged successfully.
     */
    val prMergedOn: Instant
)

/**
 * Stats for a author by specific [reviewerId].
 */
class AuthorReviewStats(
    val repoId: String,
    val prAuthorId: UserId,
    val reviewerId: UserId,
    val average: Duration,
    val totalReviews: Int,
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
    val reviewComment: Int
) {
    val allComments: Int = issueComment + reviewComment

    override fun toString(): String {
        return "$user made $issueComment PR comment and $reviewComment review comment. Total: $allComments comments."
    }
}
