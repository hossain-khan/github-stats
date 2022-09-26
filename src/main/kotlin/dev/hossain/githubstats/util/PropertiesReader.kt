package dev.hossain.githubstats.util

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
                properties.load(File("local_sample.properties").inputStream())
            } else {
                throw IllegalStateException("Please create `local.properties` with config values. See `local_sample.properties`.")
            }
        }
    }

    fun getProperty(key: String): String = properties.getProperty(key)
}

class LocalProperties : PropertiesReader("local.properties") {
    companion object {
        private const val KEY_REPO_OWNER = "repository_owner"
        private const val KEY_REPO_ID = "repository_id"
    }

    fun getRepoOwner(): String = getProperty(KEY_REPO_OWNER)
    fun getRepoId(): String = getProperty(KEY_REPO_ID)
}
