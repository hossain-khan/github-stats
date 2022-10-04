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
    val reviewTime: Map<String, Duration>,
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
