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
    fun diffWorkingHours() {
        // Sat Jan 01 2022 10:00:00 GMT-0500 (Eastern Standard Time)
        // Sat Jan 01 2022 15:00:00 GMT+0000
        val startTime: Instant = Instant.fromEpochSeconds(1641049200)

        // Mon Jan 03 2022 14:00:00 GMT-0500 (Eastern Standard Time)
        // Mon Jan 03 2022 19:00:00 GMT+0000
        val endTime: Instant = Instant.fromEpochSeconds(1641236400)

        // America/Toronto
        val zoneId = ZoneId.systemDefault()

        DateTimeDiffer.diffWorkingHours(startTime, endTime, zoneId)
    }
}
