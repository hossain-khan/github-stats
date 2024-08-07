package dev.hossain.githubstats.util

import dev.hossain.githubstats.AppConstants.LOCAL_PROPERTIES_FILE
import dev.hossain.githubstats.AppConstants.LOCAL_PROPERTIES_SAMPLE_FILE
import java.io.File
import java.util.Properties

/**
 * Internal class to get properties
 */
abstract class PropertiesReader(
    fileName: String,
) {
    private val properties = Properties()

    init {
        val propertiesFile = File(fileName)
        if (propertiesFile.exists()) {
            properties.load(propertiesFile.inputStream())
        } else {
            if (System.getenv("IS_GITHUB_CI") == "true") {
                properties.load(File(LOCAL_PROPERTIES_SAMPLE_FILE).inputStream())
            } else {
                throw IllegalStateException(
                    "Please create `$LOCAL_PROPERTIES_FILE` with config values. See `$LOCAL_PROPERTIES_SAMPLE_FILE`.",
                )
            }
        }
    }

    fun getProperty(key: String): String? = properties.getProperty(key)
}

class LocalProperties : PropertiesReader(LOCAL_PROPERTIES_FILE) {
    companion object {
        private const val KEY_REPO_OWNER = "repository_owner"
        private const val KEY_REPO_ID = "repository_id"
        private const val KEY_AUTHOR_IDS = "authors"
        private const val KEY_BOT_USERS = "bot_users"
        private const val KEY_DATE_LIMIT_AFTER = "date_limit_after"
        private const val KEY_DATE_LIMIT_BEFORE = "date_limit_before"
    }

    fun getRepoOwner(): String =
        requireNotNull(getProperty(KEY_REPO_OWNER)) {
            "Repository owner also known as Org ID config is required in $LOCAL_PROPERTIES_FILE"
        }

    fun getRepoId(): String =
        requireNotNull(getProperty(KEY_REPO_ID)) {
            "Repository ID config is required in $LOCAL_PROPERTIES_FILE"
        }

    fun getAuthors(): String? = getProperty(KEY_AUTHOR_IDS)

    fun getBotUsers(): String? = getProperty(KEY_BOT_USERS)

    fun getDateLimitAfter(): String? = getProperty(KEY_DATE_LIMIT_AFTER)

    fun getDateLimitBefore(): String? = getProperty(KEY_DATE_LIMIT_BEFORE)
}
