package dev.hossain.time

import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import java.time.ZonedDateTime

/**
 * Extension function to convert [Instant] to [ZonedDateTime] at fixed time zone.
 */
fun Instant.toZdt(): ZonedDateTime {
    val date1JavaInstant: java.time.Instant = this.toJavaInstant()
    return date1JavaInstant.atZone(Zone.city("New York"))
}

/**
 * Extension function to pretty print [Instant] for fixed time zone.
 */
fun Instant.print(): String {
    return this.toZdt().format()
}
