package time

import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.time.Duration

object DateTimeDiffer {
    fun diffWorkingHours(startTime: Instant, endTime: Instant, zoneId: ZoneId): Duration {
        val startDateTime: ZonedDateTime = startTime.toJavaInstant().atZone(zoneId)
        val endDateTime: ZonedDateTime = endTime.toJavaInstant().atZone(zoneId)

        println("startDateTime=$startDateTime and $endDateTime")

        return endTime - startTime
    }
}
