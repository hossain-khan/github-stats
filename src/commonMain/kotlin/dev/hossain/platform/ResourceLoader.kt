package dev.hossain.platform

/**
 * Expected functionality for loading localized resources.
 */
expect class ExpectedResourceBundle {
    fun getString(key: String): String
    // Potentially add methods for handling different locales if needed
}

/**
 * Factory function to get an instance of the expected resource bundle.
 * @param baseBundleName The base name of the resource bundle (e.g., "strings").
 * @param localeTag The IETF BCP 47 language tag string (e.g., "en-US").
 */
expect fun getResourceBundle(baseBundleName: String, localeTag: String): ExpectedResourceBundle
