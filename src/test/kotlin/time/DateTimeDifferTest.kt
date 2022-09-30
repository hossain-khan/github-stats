package time

import kotlinx.datetime.Instant
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.ZoneId

internal class DateTimeDifferTest {

    @BeforeEach
    fun setUp() {
    }

    @AfterEach
    fun tearDown() {
    }

    @Test
    fun `diffWorkingHours - given start time is weekends`() {
        // Sat Jan 01 2022 10:00:00 GMT-0500 (Eastern Standard Time)
        // Sat Jan 01 2022 15:00:00 GMT+0000
        val startTime: Instant = Instant.fromEpochSeconds(1641049200)

        // Mon Jan 03 2022 14:00:00 GMT-0500 (Eastern Standard Time)
        // Mon Jan 03 2022 19:00:00 GMT+0000
        val endTime: Instant = Instant.fromEpochSeconds(1641236400)

        // America/Toronto
        val zoneId = ZoneId.systemDefault()

        val diffWorkingHours = DateTimeDiffer.diffWorkingHours(startTime, endTime, zoneId)
        println("RESULT: diffWorkingHours() = $diffWorkingHours")
    }

    @Test
    fun `diffWorkingHours - given start time is before work hour beings`() {
        // Fri Jan 07 2022 03:00:00 GMT-0500 (Eastern Standard Time)
        // Fri Jan 07 2022 08:00:00 GMT+0000
        val startTime: Instant = Instant.fromEpochSeconds(1641542400)

        // Fri Jan 07 2022 15:00:00 GMT-0500 (Eastern Standard Time)
        // Fri Jan 07 2022 20:00:00 GMT+0000
        val endTime: Instant = Instant.fromEpochSeconds(1641585600)

        // America/Toronto
        val zoneId = ZoneId.systemDefault()

        val diffWorkingHours = DateTimeDiffer.diffWorkingHours(startTime, endTime, zoneId)
        println("RESULT: diffWorkingHours() = $diffWorkingHours")
    }

    @Test
    fun `diffWorkingHours - given start time is after work hour ends`() {
        // Wed Jan 05 2022 21:00:00 GMT-0500 (Eastern Standard Time)
        // Thu Jan 06 2022 02:00:00 GMT+0000
        val startTime: Instant = Instant.fromEpochSeconds(1641434400)

        // Thu Jan 06 2022 14:00:00 GMT-0500 (Eastern Standard Time)
        // Thu Jan 06 2022 19:00:00 GMT+0000
        val endTime: Instant = Instant.fromEpochSeconds(1641495600)

        // America/Toronto
        val zoneId = ZoneId.systemDefault()

        val diffWorkingHours = DateTimeDiffer.diffWorkingHours(startTime, endTime, zoneId)
        println("RESULT: diffWorkingHours() = $diffWorkingHours")
    }
}
