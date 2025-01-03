package dev.hossain.githubstats

import dev.hossain.githubstats.logging.Log

/**
 * Internal build config.
 * NOTE: Take a look at https://github.com/gmazzo/gradle-buildconfig-plugin
 */
object BuildConfig {
    /**
     * Set logging level to see different logs as the stat is being generated.
     * @see Log.VERBOSE
     * @see Log.DEBUG
     * @see Log.INFO
     * @see Log.WARNING
     * @see Log.NONE
     */
    var logLevel = Log.DEBUG

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
    const val API_REQUEST_DELAY_MS: Long = 200L

    /**
     * HTTP requests are cached locally to re-used responses that has not changed.
     * Configuration for max size on disk to cache response files.
     */
    const val HTTP_CACHE_SIZE: Long = 100L * 1024L * 1024L // 100 MB

    /**
     * Configuration for providing progress update after each `N` number of PR analysis.
     */
    const val PROGRESS_UPDATE_SPAN = 10

    /**
     * Error threshold to stop processing further if same error count exceeds this limit.
     */
    const val ERROR_THRESHOLD = 10
}
