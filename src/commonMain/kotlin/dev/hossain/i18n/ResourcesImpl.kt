package dev.hossain.i18n

import dev.hossain.platform.ExpectedResourceBundle

/**
 * Localized resource provider using common [ExpectedResourceBundle].
 * @see Resources
 */
class ResourcesImpl constructor(
    private val resourceBundle: ExpectedResourceBundle,
) : Resources {
    override fun string(
        key: String,
        vararg args: Any?,
    ): String {
        val rawString = resourceBundle.getString(key)
        // Basic substitution for common case.
        // Actual ExpectedResourceBundle can implement more sophisticated formatting.
        if (args.isEmpty()) {
            return rawString
        }
        // This is a very basic placeholder for formatting.
        // A proper solution would involve an expect/actual for String.format
        // or using a KMP-friendly formatting library.
        // For now, let's assume simple sequential replacement for %s or similar.
        // This will likely need refinement.
        var formattedString = rawString
        args.forEach { arg ->
            // Assuming %s like placeholders. This is naive.
            // A better approach is needed if complex formatting is used.
            formattedString = formattedString.replaceFirst("%s", arg.toString())
        }
        return formattedString
    }
}
