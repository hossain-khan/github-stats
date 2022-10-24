package dev.hossain.githubstats.util

import dev.hossain.githubstats.AppConstants.LOCAL_PROPERTIES_FILE
import dev.hossain.githubstats.AppConstants.LOCAL_PROPERTIES_SAMPLE_FILE
import java.io.File
import java.util.Properties

/**
 * Internal class to get properties
 */
abstract class PropertiesReader(fileName: String) {
    private val properties = Properties()

    init {
        val propertiesFile = File(fileName)
        if (propertiesFile.exists()) {
            properties.load(propertiesFile.inputStream())
        } else {
            if (System.getenv("IS_GITHUB_CI") == "true") {
                properties.load(File(LOCAL_PROPERTIES_SAMPLE_FILE).inputStream())
            } else {
                throw IllegalStateException("Please create `$LOCAL_PROPERTIES_FILE` with config values. See `$LOCAL_PROPERTIES_SAMPLE_FILE`.")
            }
        }
    }

    fun getProperty(key: String): String = properties.getProperty(key)
}

class LocalProperties : PropertiesReader(LOCAL_PROPERTIES_FILE) {
    companion object {
        private const val KEY_REPO_OWNER = "repository_owner"
        private const val KEY_REPO_ID = "repository_id"
        private const val KEY_AUTHOR_IDS = "authors"
        private const val KEY_DATE_LIMIT = "date_limit"
    }

    fun getRepoOwner(): String = getProperty(KEY_REPO_OWNER)
    fun getRepoId(): String = getProperty(KEY_REPO_ID)
    fun getAuthors(): String = getProperty(KEY_AUTHOR_IDS)
    fun getDateLimit(): String = getProperty(KEY_DATE_LIMIT)
}
