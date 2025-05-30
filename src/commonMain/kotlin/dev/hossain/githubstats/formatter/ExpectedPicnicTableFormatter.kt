package dev.hossain.githubstats.formatter

/**
 * Expected class for PicnicTableFormatter that implements StatsFormatter.
 * The actual implementation on JVM will use the Picnic library.
 */
expect class PicnicTableFormatter constructor() : StatsFormatter {
    override fun formatSinglePrStats(prStats: dev.hossain.githubstats.PrStats): String
    override fun formatAuthorStats(stats: dev.hossain.githubstats.AuthorStats): String
    override fun formatAllAuthorStats(aggregatedPrStats: List<dev.hossain.githubstats.AuthorPrStats>): String
    override fun formatReviewerStats(stats: dev.hossain.githubstats.ReviewerReviewStats): String
}
