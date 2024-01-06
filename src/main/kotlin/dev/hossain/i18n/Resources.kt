package dev.hossain.i18n

/**
 * Application resource provider.
 */
interface Resources {
    /**
     * Provides localized string for given key formatted with [args] if provided.
     * @see String.format
     */
    fun string(
        key: String,
        vararg args: Any?,
    ): String
}
