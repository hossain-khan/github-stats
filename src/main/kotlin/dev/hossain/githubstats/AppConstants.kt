package dev.hossain.githubstats

/**
 * Common app constants that are used throughout the app.
 */
object AppConstants {
    /**
     * Constant for [BuildConfig] file name reference.
     */
    val BUILD_CONFIG: String = BuildConfig::class.java.simpleName

    /**
     * Constant for properties file containing all configs for the app.
     * Rename [LOCAL_PROPERTIES_SAMPLE_FILE] file or duplicate it
     * and name this as [LOCAL_PROPERTIES_FILE] to provide configs for the app.
     */
    const val LOCAL_PROPERTIES_FILE = "local.properties"

    /**
     * Constant for sample properties file provided with the project.
     * See at the root of the project directory.
     */
    const val LOCAL_PROPERTIES_SAMPLE_FILE = "local_sample.properties"

    /**
     * Label for PR analysis progress bar.
     */
    const val PROGRESS_LABEL = "Progress"

    /**
     * GitHub token settings URL to direct user to create a new token.
     */
    const val GITHUB_TOKEN_SETTINGS_URL = "https://github.com/settings/tokens"

    /**
     * Default timeout in seconds for GH CLI command execution.
     * This prevents indefinite hangs when GH CLI is unresponsive or network issues occur.
     */
    const val GH_CLI_DEFAULT_TIMEOUT_SECONDS = 10L
}
