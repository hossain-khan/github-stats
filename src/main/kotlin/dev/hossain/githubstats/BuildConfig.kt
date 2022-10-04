package dev.hossain.githubstats

/**
 * Internal build config.
 * NOTE: Take a look at https://github.com/gmazzo/gradle-buildconfig-plugin
 */
object BuildConfig {
    /**
     * Shows debug information about stat collection.
     */
    const val DEBUG = true

    /**
     * Shows HTTP requests and response on the console.
     */
    const val DEBUG_HTTP_REQUESTS = false

    /**
     * Delay between consecutive API requests made to avoid being rate-limited or throttled.
     *
     * User-to-server requests are limited to 5,000 requests per hour and per authenticated user.
     * See https://docs.github.com/en/rest/overview/resources-in-the-rest-api#rate-limiting
     */
    const val API_REQUEST_DELAY_MS: Long = 20L
}
