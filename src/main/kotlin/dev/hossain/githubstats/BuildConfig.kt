package dev.hossain.githubstats

/**
 * Internal build config.
 */
object BuildConfig {
    /**
     * Shows debug log for HTTP and other operational actions.
     */
    const val DEBUG = false

    /**
     * Delay between consecutive API requests made to avoid being rate-limited or throttled.
     *
     * User-to-server requests are limited to 5,000 requests per hour and per authenticated user.
     * See https://docs.github.com/en/rest/overview/resources-in-the-rest-api#rate-limiting
     */
    const val API_REQUEST_DELAY_MS: Long = 100L
}
