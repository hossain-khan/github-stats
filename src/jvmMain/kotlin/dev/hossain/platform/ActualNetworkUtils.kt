package dev.hossain.platform

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * Actual JVM implementation for URL encoding.
 */
actual fun urlEncode(value: String): String {
    return URLEncoder.encode(value, StandardCharsets.UTF_8.name())
}
