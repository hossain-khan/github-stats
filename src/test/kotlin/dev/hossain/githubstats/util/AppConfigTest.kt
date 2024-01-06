package dev.hossain.githubstats.util

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle
import java.util.Locale

/**
 * Unit test for [AppConfig].
 */
class AppConfigTest {
    private lateinit var localProperties: LocalProperties
    private lateinit var appConfig: AppConfig

    @BeforeEach
    fun setUp() {
        localProperties = mockk(relaxed = true)
    }

    @Test
    fun `get should return Config with correct values`() {
        every { localProperties.getRepoOwner() } returns "owner"
        every { localProperties.getRepoId() } returns "id"
        every { localProperties.getDateLimitAfter() } returns "2022-01-01"
        every { localProperties.getDateLimitBefore() } returns "2022-12-31"
        every { localProperties.getAuthors() } returns "author1,author2"

        appConfig = AppConfig(localProperties)
        val config = appConfig.get()

        assertEquals("owner", config.repoOwner)
        assertEquals("id", config.repoId)
        assertEquals(listOf("author1", "author2"), config.userIds)
        assertEquals("2022-01-01", config.dateLimitAfter)
        assertEquals("2022-12-31", config.dateLimitBefore)
    }

    @Test
    fun `requireUser should throw exception when authors is null`() {
        every { localProperties.getRepoOwner() } returns "owner"
        every { localProperties.getRepoId() } returns "id"
        every { localProperties.getDateLimitAfter() } returns "2022-01-01"
        every { localProperties.getDateLimitBefore() } returns "2022-12-31"
        every { localProperties.getAuthors() } returns null

        assertThrows<IllegalArgumentException> {
            appConfig = AppConfig(localProperties)
        }
    }

    @Test
    fun `requireUser should throw exception when authors is empty`() {
        every { localProperties.getRepoOwner() } returns "owner"
        every { localProperties.getRepoId() } returns "id"
        every { localProperties.getDateLimitAfter() } returns "2022-01-01"
        every { localProperties.getDateLimitBefore() } returns "2022-12-31"
        every { localProperties.getAuthors() } returns ""

        assertThrows<IllegalArgumentException> {
            appConfig = AppConfig(localProperties)
        }
    }

    @Test
    fun `requiredValidDateOrDefault should return today's date when dateText is null`() {
        every { localProperties.getRepoOwner() } returns "owner"
        every { localProperties.getRepoId() } returns "id"
        every { localProperties.getDateLimitAfter() } returns "2022-01-01"
        every { localProperties.getAuthors() } returns "author1,author2"
        every { localProperties.getDateLimitBefore() } returns null

        val todayDate =
            DateTimeFormatter.ofPattern("uuuu-MM-dd", Locale.US)
                .withResolverStyle(ResolverStyle.STRICT)
                .format(LocalDate.now())

        appConfig = AppConfig(localProperties)
        val config = appConfig.get()

        assertEquals(todayDate, config.dateLimitBefore)
    }

    @Test
    fun `requiredValidDateOrDefault should throw exception when dateText is not valid`() {
        every { localProperties.getRepoOwner() } returns "owner"
        every { localProperties.getRepoId() } returns "id"
        every { localProperties.getDateLimitAfter() } returns "2022-01-01"
        every { localProperties.getAuthors() } returns "author1,author2"

        every { localProperties.getDateLimitBefore() } returns "invalid-date"

        assertThrows<IllegalArgumentException> {
            appConfig = AppConfig(localProperties)
            appConfig.get()
        }
    }

    @Test
    fun `requireValidDate should throw exception when dateText is null`() {
        every { localProperties.getDateLimitAfter() } returns null

        assertThrows<IllegalArgumentException> {
            appConfig = AppConfig(localProperties)
            appConfig.get()
        }
    }

    @Test
    fun `requireValidDate should throw exception when dateText is not valid`() {
        every { localProperties.getRepoOwner() } returns "owner"
        every { localProperties.getRepoId() } returns "id"
        every { localProperties.getDateLimitBefore() } returns "2022-12-31"
        every { localProperties.getAuthors() } returns "author1,author2"

        every { localProperties.getDateLimitAfter() } returns "invalid-date"

        assertThrows<IllegalArgumentException> {
            appConfig = AppConfig(localProperties)
            appConfig.get()
        }
    }
}
