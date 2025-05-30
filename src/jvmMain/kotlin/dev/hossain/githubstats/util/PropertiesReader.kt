package dev.hossain.githubstats.util

import dev.hossain.githubstats.AppConstants
import java.io.File
import java.util.Properties

actual class PropertiesReader actual constructor(fileName: String) {
    private val properties = Properties()

    init {
        val propertiesFile = File(fileName)
        if (propertiesFile.exists()) {
            properties.load(propertiesFile.inputStream())
        } else {
            // Fallback for CI environment or if local.properties is missing
            // but local_sample.properties exists.
            val sampleFile = File(AppConstants.LOCAL_PROPERTIES_SAMPLE_FILE)
            if (System.getenv("IS_GITHUB_CI") == "true" && sampleFile.exists()) {
                properties.load(sampleFile.inputStream())
            } else if (fileName == AppConstants.LOCAL_PROPERTIES_FILE && sampleFile.exists()) {
                // If the main properties file is not found, but sample exists (non-CI)
                // it's better to throw error to prompt user to create local.properties
                throw IllegalStateException(
                    "Please create `${AppConstants.LOCAL_PROPERTIES_FILE}` with config values. " +
                        "You can duplicate and rename `${AppConstants.LOCAL_PROPERTIES_SAMPLE_FILE}`."
                )
            } else if (!propertiesFile.exists()) {
                // If even the specified fileName doesn't exist (and it's not the default one with a sample)
                throw IllegalStateException("Properties file `$fileName` not found.")
            }
        }
    }

    actual fun getProperty(key: String): String? = properties.getProperty(key)

    actual fun getProperty(key: String, defaultValue: String): String = properties.getProperty(key, defaultValue)

    // Constants for property keys, mimicking LocalProperties
    companion object {
        private const val KEY_REPO_OWNER = "repository_owner"
        private const val KEY_REPO_ID = "repository_id"
        private const val KEY_AUTHOR_IDS = "authors"
        private const val KEY_BOT_USERS = "bot_users"
        private const val KEY_DATE_LIMIT_AFTER = "date_limit_after"
        private const val KEY_DATE_LIMIT_BEFORE = "date_limit_before"
    }

    actual fun getRepoOwner(): String =
        requireNotNull(getProperty(KEY_REPO_OWNER)) {
            "Repository owner (repository_owner) config is required in properties file."
        }

    actual fun getRepoId(): String =
        requireNotNull(getProperty(KEY_REPO_ID)) {
            "Repository ID (repository_id) config is required in properties file."
        }

    actual fun getAuthors(): String? = getProperty(KEY_AUTHOR_IDS)
    actual fun getBotUsers(): String? = getProperty(KEY_BOT_USERS)
    actual fun getDateLimitAfter(): String? = getProperty(KEY_DATE_LIMIT_AFTER)
    actual fun getDateLimitBefore(): String? = getProperty(KEY_DATE_LIMIT_BEFORE)
}
