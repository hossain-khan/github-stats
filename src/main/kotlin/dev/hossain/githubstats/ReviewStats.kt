package dev.hossain.githubstats

import dev.hossain.githubstats.model.PullRequest
import kotlinx.datetime.Instant
import kotlin.time.Duration

data class ReviewStats(
    val pullRequest: PullRequest,
    val reviewCompletion: Duration,
    val prReadyOn: Instant,
    val prMergedOn: Instant
)

data class AuthorReviewStats(
    val repoId: String,
    val prAuthorId: UserId,
    val reviewerId: UserId,
    val average: Duration,
    val totalReviews: Int,
    val stats: List<ReviewStats>
)
