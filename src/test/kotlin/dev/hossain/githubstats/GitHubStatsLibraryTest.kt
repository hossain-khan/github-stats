package dev.hossain.githubstats

import dev.hossain.githubstats.logging.Log
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

/**
 * Tests for [GitHubStatsLibrary].
 */
class GitHubStatsLibraryTest {
    @Test
    fun `library can be instantiated`() {
        val library = GitHubStatsLibrary()
        assertNotNull(library)
    }

    @Test
    fun `config can be created with required parameters`() {
        val config =
            GitHubStatsConfig(
                githubToken = "test_token",
                repoOwner = "test_owner",
                repoName = "test_repo",
                userIds = listOf("test_user"),
            )

        assertNotNull(config)
        assert(config.githubToken == "test_token")
        assert(config.repoOwner == "test_owner")
        assert(config.repoName == "test_repo")
        assert(config.userIds == listOf("test_user"))
        assert(config.logLevel == Log.INFO) // default value
    }

    @Test
    fun `config can be created with all parameters`() {
        val config =
            GitHubStatsConfig(
                githubToken = "test_token",
                repoOwner = "test_owner",
                repoName = "test_repo",
                userIds = listOf("test_user1", "test_user2"),
                dateAfter = "2023-01-01",
                dateBefore = "2023-12-31",
                botUserIds = listOf("bot1", "bot2"),
                logLevel = Log.DEBUG,
            )

        assertNotNull(config)
        assert(config.dateAfter == "2023-01-01")
        assert(config.dateBefore == "2023-12-31")
        assert(config.botUserIds == listOf("bot1", "bot2"))
        assert(config.logLevel == Log.DEBUG)
    }

    @Test
    fun `library throws exception when not initialized`() {
        val library = GitHubStatsLibrary()

        assertThrows(IllegalStateException::class.java) {
            library.generateAuthorStats()
        }

        assertThrows(IllegalStateException::class.java) {
            library.generateReviewerStats()
        }

        assertThrows(IllegalStateException::class.java) {
            library.generateAllStats()
        }
    }
}
