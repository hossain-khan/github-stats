package dev.hossain.i18n

import java.util.ResourceBundle

/**
 * Localized resource provider.
 * @see Resources
 */
class ResourcesImpl constructor(
    private val resourceBundle: ResourceBundle,
) : Resources {
    override fun string(
        key: String,
        vararg args: Any?,
    ): String {
        return String.format(resourceBundle.getString(key), *args)
    }
}
