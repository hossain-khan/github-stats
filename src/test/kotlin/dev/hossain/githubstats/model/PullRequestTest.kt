package dev.hossain.githubstats.model

import com.google.common.truth.Truth.assertThat
import kotlinx.datetime.Instant
import org.junit.jupiter.api.Test

class PullRequestTest {

    private val testUser = User(
        login = "testuser",
        type = "User",
        url = "https://api.github.com/users/testuser",
        html_url = "https://github.com/testuser",
        avatar_url = "https://avatars.githubusercontent.com/u/12345?v=4",
        id = 12345L,
        repos_url = "https://api.github.com/users/testuser/repos"
    )

    @Test
    fun `PullRequest should store all provided values`() {
        val pr = PullRequest(
            id = 98765L,
            number = 123,
            state = "open",
            title = "Test Pull Request",
            url = "https://api.github.com/repos/owner/repo/pulls/123",
            html_url = "https://github.com/owner/repo/pull/123",
            user = testUser,
            merged = false,
            created_at = "2023-01-01T10:00:00Z",
            updated_at = "2023-01-01T11:00:00Z",
            closed_at = null,
            merged_at = null
        )

        assertThat(pr.id).isEqualTo(98765L)
        assertThat(pr.number).isEqualTo(123)
        assertThat(pr.state).isEqualTo("open")
        assertThat(pr.title).isEqualTo("Test Pull Request")
        assertThat(pr.url).isEqualTo("https://api.github.com/repos/owner/repo/pulls/123")
        assertThat(pr.html_url).isEqualTo("https://github.com/owner/repo/pull/123")
        assertThat(pr.user).isEqualTo(testUser)
        assertThat(pr.merged).isFalse()
        assertThat(pr.created_at).isEqualTo("2023-01-01T10:00:00Z")
        assertThat(pr.updated_at).isEqualTo("2023-01-01T11:00:00Z")
        assertThat(pr.closed_at).isNull()
        assertThat(pr.merged_at).isNull()
    }

    @Test
    fun `isMerged should be true when merged is true and merged_at is not null`() {
        val pr = PullRequest(
            id = 1L, number = 1, state = "closed", title = "Merged PR", url = "", html_url = "", user = testUser,
            merged = true,
            created_at = "2023-01-01T10:00:00Z",
            updated_at = "2023-01-01T12:00:00Z",
            closed_at = "2023-01-01T12:00:00Z",
            merged_at = "2023-01-01T12:00:00Z"
        )
        assertThat(pr.isMerged).isTrue()
    }

    @Test
    fun `isMerged should be false when merged is false`() {
        val pr = PullRequest(
            id = 2L, number = 2, state = "closed", title = "Closed PR", url = "", html_url = "", user = testUser,
            merged = false,
            created_at = "2023-01-01T10:00:00Z",
            updated_at = "2023-01-01T12:00:00Z",
            closed_at = "2023-01-01T12:00:00Z",
            merged_at = null
        )
        assertThat(pr.isMerged).isFalse()
    }

    @Test
    fun `isMerged should be false when merged is null`() {
        val pr = PullRequest(
            id = 3L, number = 3, state = "open", title = "Open PR", url = "", html_url = "", user = testUser,
            merged = null, // Github API can return null for merged when PR is open
            created_at = "2023-01-01T10:00:00Z",
            updated_at = null, closed_at = null, merged_at = null
        )
        assertThat(pr.isMerged).isFalse()
    }

    @Test
    fun `prCreatedOn should parse created_at string to Instant`() {
        val createdAtString = "2023-01-15T14:30:45Z"
        val pr = PullRequest(
            id = 1L, number = 1, state = "open", title = "Test PR", url = "", html_url = "", user = testUser,
            merged = false, created_at = createdAtString, updated_at = null, closed_at = null, merged_at = null
        )
        assertThat(pr.prCreatedOn).isEqualTo(Instant.parse(createdAtString))
    }

    @Test
    fun `prMergedOn should parse merged_at string to Instant when not null`() {
        val mergedAtString = "2023-01-16T09:00:00Z"
        val pr = PullRequest(
            id = 1L, number = 1, state = "closed", title = "Test PR", url = "", html_url = "", user = testUser,
            merged = true, created_at = "2023-01-15T10:00:00Z", updated_at = null,
            closed_at = mergedAtString, merged_at = mergedAtString
        )
        assertThat(pr.prMergedOn).isEqualTo(Instant.parse(mergedAtString))
    }

    @Test
    fun `prMergedOn should be null when merged_at is null`() {
        val pr = PullRequest(
            id = 1L, number = 1, state = "open", title = "Test PR", url = "", html_url = "", user = testUser,
            merged = false, created_at = "2023-01-15T10:00:00Z", updated_at = null, closed_at = null, merged_at = null
        )
        assertThat(pr.prMergedOn).isNull()
    }

    @Test
    fun `PullRequest should handle null values for optional string fields`() {
        val pr = PullRequest(
            id = 98765L,
            number = 123,
            state = "open",
            title = "Test Pull Request",
            url = "https://api.github.com/repos/owner/repo/pulls/123",
            html_url = "https://github.com/owner/repo/pull/123",
            user = testUser,
            merged = null, // API returns null for 'merged' if PR is not merged (still open)
            created_at = "2023-01-01T10:00:00Z",
            updated_at = null,
            closed_at = null,
            merged_at = null
        )

        assertThat(pr.updated_at).isNull()
        assertThat(pr.closed_at).isNull()
        assertThat(pr.merged_at).isNull()
        assertThat(pr.merged).isNull() // Check that merged can be null
        assertThat(pr.isMerged).isFalse() // isMerged should be false if merged is null
    }
}
