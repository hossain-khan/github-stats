package time

import com.google.common.truth.Truth.assertThat
import kotlinx.datetime.Instant
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.ZoneId
import kotlin.time.Duration

internal class DateTimeDifferTest {

    @BeforeEach
    fun setUp() {
    }

    @AfterEach
    fun tearDown() {
    }

    // - https://timestampgenerator.com/

    @Test
    fun `diff - given start time is after end time - throws error`() {
        val startTime: Instant = Instant.parse("2022-09-05T11:30:00-05:00")
        val endTime: Instant = Instant.parse("2022-09-05T10:00:00-05:00")

        Assertions.assertThrows(IllegalArgumentException::class.java) {
            DateTimeDiffer.diffWorkingHours(startTime, endTime, ZoneId.systemDefault())
        }
    }

    @Test
    fun `diff - given start time and end time both in working day and hour - provides right diff`() {
        val startTime: Instant = Instant.parse("2022-09-05T10:00:00-05:00")
        val endTime: Instant = Instant.parse("2022-09-05T11:30:00-05:00")

        // America/Toronto
        val zoneId = ZoneId.systemDefault()

        val diffWorkingHours = DateTimeDiffer.diffWorkingHours(startTime, endTime, zoneId)

        assertThat(diffWorkingHours).isEqualTo("1h 30m".duration())
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

    private fun String.duration(): Duration = Duration.parse(this)
}
