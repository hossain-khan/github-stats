package dev.hossain.time

import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import java.time.ZonedDateTime

/**
 * Extension function to convert [Instant] to [ZonedDateTime] at fixed time zone.
 */
fun Instant.toZdt(): ZonedDateTime {
    val javaInstant: java.time.Instant = this.toJavaInstant()
    return javaInstant.atZone(Zone.city("New York"))
}

/**
 * Extension function to pretty print [Instant] for fixed time zone.
 */
fun Instant.format(): String {
    return this.toZdt().format()
}
