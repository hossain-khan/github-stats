package dev.hossain.githubstats.util

/**
 * Expected functionality for reading properties from a configuration file.
 * The constructor takes the file name (e.g., "local.properties").
 * The actual implementation will handle loading this file from the platform's file system.
 */
expect class PropertiesReader(fileName: String) {
    fun getProperty(key: String): String?
    fun getProperty(key: String, defaultValue: String): String

    // Convenience functions that were in LocalProperties
    fun getRepoOwner(): String
    fun getRepoId(): String
    fun getAuthors(): String?
    fun getBotUsers(): String?
    fun getDateLimitAfter(): String?
    fun getDateLimitBefore(): String?
}

// The LocalProperties class that previously inherited from PropertiesReader
// will now be effectively merged into the concept of PropertiesReader for commonMain.
// The actual jvmMain PropertiesReader will handle the file loading and specific key lookups.
// Or, if LocalProperties is still needed as a distinct concept using the common PropertiesReader,
// it would become a common class that takes an `expect PropertiesReader` instance.
// For now, let's assume the expect PropertiesReader directly provides these get methods.
// This simplifies Koin, as AppConfig was taking LocalProperties, which was a PropertiesReader.
// So AppConfig can now take the expect PropertiesReader directly.
// The Koin binding will be for `PropertiesReader`.
