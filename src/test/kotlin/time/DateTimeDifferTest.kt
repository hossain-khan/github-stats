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
    /*
     * System default on my machine is: `America/Toronto`
     * Currently Eastern Daylight Time (EDT), UTC -4
     * Standard time (Eastern Standard Time (EST), UTC -5) starts Nov. 6, 2022
     */
    private val zoneId: ZoneId = ZoneId.of("America/Toronto")

    @BeforeEach
    fun setUp() {
    }

    @AfterEach
    fun tearDown() {
    }

    // - Sample: Mon, 05 Sep 2022 10:00:00 -0400 @ https://timestampgenerator.com/1662386400/-04:00

    @Test
    fun `diff - given start time is after end time - throws error`() {
        val startTime: Instant = Instant.parse("2022-09-05T11:30:00-04:00")
        val endTime: Instant = Instant.parse("2022-09-05T10:00:00-04:00")

        Assertions.assertThrows(IllegalArgumentException::class.java) {
            DateTimeDiffer.diffWorkingHours(startTime, endTime, ZoneId.systemDefault())
        }
    }

    @Test
    fun `diff - given start time and end time both in working day and hour - provides right diff`() {
        val startTime: Instant = Instant.parse("2022-09-05T10:00:00-04:00") // 10:00am
        val endTime: Instant = Instant.parse("2022-09-05T11:30:00-04:00") // 11:30am same day

        val diffWorkingHours = DateTimeDiffer.diffWorkingHours(startTime, endTime, zoneId)

        assertThat(diffWorkingHours).isEqualTo("1h 30m".duration())
    }

    @Test
    fun `diff - given start time and end time both in non-working day but in working hour - provides zero working hour`() {
        val startTime: Instant = Instant.parse("2022-09-03T10:00:00-04:00") // 10:00am Saturday
        val endTime: Instant = Instant.parse("2022-09-03T11:30:00-04:00") // 11:30am same day

        val diffWorkingHours = DateTimeDiffer.diffWorkingHours(startTime, endTime, zoneId)

        assertThat(diffWorkingHours).isEqualTo("0s".duration())
    }

    @Test
    fun `diff - given start time and end time both in non-working day and hour and spans two days - provides zero working hour`() {
        val startTime: Instant = Instant.parse("2022-09-03T10:00:00-04:00") // 10:00am Saturday
        val endTime: Instant = Instant.parse("2022-09-04T20:00:00-04:00") // 8:00pm Sunday

        val diffWorkingHours = DateTimeDiffer.diffWorkingHours(startTime, endTime, zoneId)

        assertThat(diffWorkingHours).isEqualTo("0s".duration())
    }

    @Test
    fun `diff - given start time and end time both in working day but end time is outside working hour - provides diff excluding non-working hours`() {
        val startTime: Instant = Instant.parse("2022-09-05T10:00:00-04:00") // 10:00am
        val endTime: Instant = Instant.parse("2022-09-05T20:00:00-04:00") // 8:00pm same day

        val diffWorkingHours = DateTimeDiffer.diffWorkingHours(startTime, endTime, zoneId)

        assertThat(diffWorkingHours).isEqualTo("7h".duration())
    }

    @Test
    fun `diff - given start time and end time both in working day but start time is outside working hour - provides diff excluding non-working hours`() {
        val startTime: Instant = Instant.parse("2022-09-05T06:00:00-04:00") // 06:00am
        val endTime: Instant = Instant.parse("2022-09-05T12:00:00-04:00") // 12:00pm same day

        val diffWorkingHours = DateTimeDiffer.diffWorkingHours(startTime, endTime, zoneId)

        assertThat(diffWorkingHours).isEqualTo("3h".duration())
    }

    @Test
    fun `diff - given start time and end time both in working day but start & end time is before working hour - provides almost diff zero`() {
        val startTime: Instant = Instant.parse("2022-09-05T06:00:00-04:00") // 06:00am
        val endTime: Instant = Instant.parse("2022-09-05T07:00:00-04:00") // 07:00am same day

        val diffWorkingHours = DateTimeDiffer.diffWorkingHours(startTime, endTime, zoneId)

        assertThat(diffWorkingHours).isEqualTo("0s".duration())
    }

    @Test
    fun `diff - given start time and end time both in working day but start & end time is after working hour - provides almost diff zero`() {
        val startTime: Instant = Instant.parse("2022-09-05T18:00:00-04:00") // 06:00pm
        val endTime: Instant = Instant.parse("2022-09-05T21:00:00-04:00") // 09:00pm same day

        val diffWorkingHours = DateTimeDiffer.diffWorkingHours(startTime, endTime, zoneId)

        assertThat(diffWorkingHours).isEqualTo("0s".duration())
    }

    @Test
    fun `diff - given start time and end time both in working day but start & end time is before and after working hour - provides working hour diff`() {
        val startTime: Instant = Instant.parse("2022-09-05T06:00:00-04:00") // 06:00am
        val endTime: Instant = Instant.parse("2022-09-05T21:00:00-04:00") // 09:00pm same day

        val diffWorkingHours = DateTimeDiffer.diffWorkingHours(startTime, endTime, zoneId)

        assertThat(diffWorkingHours).isEqualTo("8h".duration())
    }

    @Test
    fun `diff - given start time in working hour and end time is next day working hour - provides diff of working hour only`() {
        val startTime: Instant = Instant.parse("2022-09-05T10:00:00-04:00") // 10:00am
        val endTime: Instant = Instant.parse("2022-09-06T10:00:00-04:00") // 10:00am next day

        val diffWorkingHours = DateTimeDiffer.diffWorkingHours(startTime, endTime, zoneId)

        assertThat(diffWorkingHours).isEqualTo("8h".duration())
    }

    @Test
    fun `diff - given start time in working hour and end time is next day after working hour - provides diff of working hour only`() {
        val startTime: Instant = Instant.parse("2022-09-05T10:00:00-04:00") // 10:00am
        val endTime: Instant = Instant.parse("2022-09-06T20:00:00-04:00") // 08:00pm next day

        val diffWorkingHours = DateTimeDiffer.diffWorkingHours(startTime, endTime, zoneId)

        assertThat(diffWorkingHours).isEqualTo("15h".duration())
    }

    @Test
    fun `diff - given start time and end time spans multiple working days - provides diff of working hour only`() {
        val startTime: Instant = Instant.parse("2022-09-05T10:00:00-04:00") // 10:00am Monday
        val endTime: Instant = Instant.parse("2022-09-08T10:00:00-04:00") // 10:00am 3 days layer

        val diffWorkingHours = DateTimeDiffer.diffWorkingHours(startTime, endTime, zoneId)

        assertThat(diffWorkingHours).isEqualTo("24h".duration())
    }

    @Test
    fun `diff - given start time and end time spans whole week - provides diff of working hour only`() {
        val startTime: Instant = Instant.parse("2022-09-05T10:00:00-04:00") // 10:00am Monday
        val endTime: Instant = Instant.parse("2022-09-09T16:00:00-04:00") // 04:00pm Friday

        val diffWorkingHours = DateTimeDiffer.diffWorkingHours(startTime, endTime, zoneId)

        assertThat(diffWorkingHours).isEqualTo("38h".duration())
    }

    private fun String.duration(): Duration = Duration.parse(this)
}
