package dev.hossain.githubstats.client

import com.google.common.truth.Truth.assertThat
import dev.hossain.githubstats.cache.DatabaseCacheService
import dev.hossain.githubstats.model.timeline.ClosedEvent
import dev.hossain.githubstats.model.timeline.CommentedEvent
import dev.hossain.githubstats.model.timeline.MergedEvent
import dev.hossain.githubstats.model.timeline.ReadyForReviewEvent
import dev.hossain.githubstats.model.timeline.ReviewRequestedEvent
import dev.hossain.githubstats.model.timeline.ReviewedEvent
import dev.hossain.githubstats.model.timeline.UnknownEvent
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull

/**
 * Unit tests for [GhCliApiClient] JSON parsing functionality.
 * Tests that Moshi adapters correctly parse various JSON responses,
 * including polymorphic types like TimelineEvent.
 */
class GhCliApiClientJsonParsingTest {
    @Test
    fun `pullRequest - parses complete PR JSON correctly`() =
        runTest {
            // Arrange
            val prJson =
                """
                {
                    "id": 123456,
                    "number": 42,
                    "state": "closed",
                    "title": "Test PR",
                    "url": "https://api.github.com/repos/owner/repo/pulls/42",
                    "html_url": "https://github.com/owner/repo/pull/42",
                    "user": {
                        "login": "testuser",
                        "id": 1,
                        "avatar_url": "https://avatars.githubusercontent.com/u/1",
                        "url": "https://api.github.com/users/testuser",
                        "html_url": "https://github.com/testuser",
                        "type": "User"
                    },
                    "merged": true,
                    "created_at": "2023-01-01T10:00:00Z",
                    "updated_at": "2023-01-02T10:00:00Z",
                    "closed_at": "2023-01-02T10:00:00Z",
                    "merged_at": "2023-01-02T10:00:00Z"
                }
                """.trimIndent()

            val mockCache = mockk<DatabaseCacheService>()
            coEvery { mockCache.getCachedResponse(any()) } returns prJson

            val client = GhCliApiClient(databaseCacheService = mockCache)

            // Act
            val result = client.pullRequest("owner", "repo", 42)

            // Assert
            assertThat(result.number).isEqualTo(42)
            assertThat(result.title).isEqualTo("Test PR")
            assertThat(result.state).isEqualTo("closed")
            assertThat(result.user.login).isEqualTo("testuser")
            assertThat(result.merged).isTrue()
            assertThat(result.merged_at).isEqualTo("2023-01-02T10:00:00Z")
        }

    @Test
    fun `timelineEvents - parses ClosedEvent correctly`() =
        runTest {
            // Arrange
            val timelineJson =
                """
                [{
                    "id": 123456,
                    "event": "closed",
                    "actor": {
                        "login": "closer",
                        "id": 2,
                        "avatar_url": "https://avatars.githubusercontent.com/u/2",
                        "url": "https://api.github.com/users/closer",
                        "html_url": "https://github.com/closer",
                        "type": "User"
                    },
                    "created_at": "2023-01-01T12:00:00Z"
                }]
                """.trimIndent()

            val mockCache = mockk<DatabaseCacheService>()
            coEvery { mockCache.getCachedResponse(any()) } returns timelineJson

            val client = GhCliApiClient(databaseCacheService = mockCache)

            // Act
            val result = client.timelineEvents("owner", "repo", 123)

            // Assert
            assertThat(result).hasSize(1)
            assertThat(result[0]).isInstanceOf(ClosedEvent::class.java)
            val closedEvent = result[0] as ClosedEvent
            assertThat(closedEvent.actor.login).isEqualTo("closer")
        }

    @Test
    fun `timelineEvents - parses MergedEvent correctly`() =
        runTest {
            // Arrange
            val timelineJson =
                """
                [{
                    "id": 123457,
                    "event": "merged",
                    "url": "https://api.github.com/repos/owner/repo/issues/events/123457",
                    "actor": {
                        "login": "merger",
                        "id": 3,
                        "avatar_url": "https://avatars.githubusercontent.com/u/3",
                        "url": "https://api.github.com/users/merger",
                        "html_url": "https://github.com/merger",
                        "type": "User"
                    },
                    "commit_id": "abc123",
                    "commit_url": "https://github.com/owner/repo/commit/abc123",
                    "created_at": "2023-01-02T12:00:00Z"
                }]
                """.trimIndent()

            val mockCache = mockk<DatabaseCacheService>()
            coEvery { mockCache.getCachedResponse(any()) } returns timelineJson

            val client = GhCliApiClient(databaseCacheService = mockCache)

            // Act
            val result = client.timelineEvents("owner", "repo", 123)

            // Assert
            assertThat(result).hasSize(1)
            assertThat(result[0]).isInstanceOf(MergedEvent::class.java)
        }

    @Test
    fun `timelineEvents - parses ReviewedEvent correctly`() =
        runTest {
            // Arrange
            val timelineJson =
                """
                [{
                    "id": 123458,
                    "event": "reviewed",
                    "user": {
                        "login": "reviewer",
                        "id": 4,
                        "avatar_url": "https://avatars.githubusercontent.com/u/4",
                        "url": "https://api.github.com/users/reviewer",
                        "html_url": "https://github.com/reviewer",
                        "type": "User"
                    },
                    "state": "approved",
                    "submitted_at": "2023-01-01T14:00:00Z",
                    "html_url": "https://github.com/owner/repo/pull/123#pullrequestreview-123458"
                }]
                """.trimIndent()

            val mockCache = mockk<DatabaseCacheService>()
            coEvery { mockCache.getCachedResponse(any()) } returns timelineJson

            val client = GhCliApiClient(databaseCacheService = mockCache)

            // Act
            val result = client.timelineEvents("owner", "repo", 123)

            // Assert
            assertThat(result).hasSize(1)
            assertThat(result[0]).isInstanceOf(ReviewedEvent::class.java)
            val reviewedEvent = result[0] as ReviewedEvent
            assertThat(reviewedEvent.user.login).isEqualTo("reviewer")
            assertThat(reviewedEvent.state.name).isEqualTo("APPROVED")
        }

    @Test
    fun `timelineEvents - parses ReviewRequestedEvent correctly`() =
        runTest {
            // Arrange
            val timelineJson =
                """
                [{
                    "id": 123459,
                    "event": "review_requested",
                    "created_at": "2023-01-01T15:00:00Z",
                    "actor": {
                        "login": "requester",
                        "id": 5,
                        "avatar_url": "https://avatars.githubusercontent.com/u/5",
                        "url": "https://api.github.com/users/requester",
                        "html_url": "https://github.com/requester",
                        "type": "User"
                    },
                    "requested_reviewer": {
                        "login": "requested_reviewer",
                        "id": 6,
                        "avatar_url": "https://avatars.githubusercontent.com/u/6",
                        "url": "https://api.github.com/users/requested_reviewer",
                        "html_url": "https://github.com/requested_reviewer",
                        "type": "User"
                    },
                    "requested_team": null,
                    "review_requester": {
                        "login": "requester",
                        "id": 5,
                        "avatar_url": "https://avatars.githubusercontent.com/u/5",
                        "url": "https://api.github.com/users/requester",
                        "html_url": "https://github.com/requester",
                        "type": "User"
                    }
                }]
                """.trimIndent()

            val mockCache = mockk<DatabaseCacheService>()
            coEvery { mockCache.getCachedResponse(any()) } returns timelineJson

            val client = GhCliApiClient(databaseCacheService = mockCache)

            // Act
            val result = client.timelineEvents("owner", "repo", 123)

            // Assert
            assertThat(result).hasSize(1)
            assertThat(result[0]).isInstanceOf(ReviewRequestedEvent::class.java)
        }

    @Test
    fun `timelineEvents - parses ReadyForReviewEvent correctly`() =
        runTest {
            // Arrange
            val timelineJson =
                """
                [{
                    "id": 123460,
                    "event": "ready_for_review",
                    "created_at": "2023-01-01T16:00:00Z",
                    "actor": {
                        "login": "author",
                        "id": 7,
                        "avatar_url": "https://avatars.githubusercontent.com/u/7",
                        "url": "https://api.github.com/users/author",
                        "html_url": "https://github.com/author",
                        "type": "User"
                    }
                }]
                """.trimIndent()

            val mockCache = mockk<DatabaseCacheService>()
            coEvery { mockCache.getCachedResponse(any()) } returns timelineJson

            val client = GhCliApiClient(databaseCacheService = mockCache)

            // Act
            val result = client.timelineEvents("owner", "repo", 123)

            // Assert
            assertThat(result).hasSize(1)
            assertThat(result[0]).isInstanceOf(ReadyForReviewEvent::class.java)
        }

    @Test
    fun `timelineEvents - parses CommentedEvent correctly`() =
        runTest {
            // Arrange
            val timelineJson =
                """
                [{
                    "event": "commented",
                    "url": "https://api.github.com/repos/owner/repo/issues/comments/123461",
                    "html_url": "https://github.com/owner/repo/pull/123#issuecomment-123461",
                    "actor": {
                        "login": "commenter",
                        "id": 8,
                        "avatar_url": "https://avatars.githubusercontent.com/u/8",
                        "url": "https://api.github.com/users/commenter",
                        "html_url": "https://github.com/commenter",
                        "type": "User"
                    },
                    "user": {
                        "login": "commenter",
                        "id": 8,
                        "avatar_url": "https://avatars.githubusercontent.com/u/8",
                        "url": "https://api.github.com/users/commenter",
                        "html_url": "https://github.com/commenter",
                        "type": "User"
                    },
                    "created_at": "2023-01-01T17:00:00Z",
                    "updated_at": "2023-01-01T17:00:00Z",
                    "body": "This is a comment"
                }]
                """.trimIndent()

            val mockCache = mockk<DatabaseCacheService>()
            coEvery { mockCache.getCachedResponse(any()) } returns timelineJson

            val client = GhCliApiClient(databaseCacheService = mockCache)

            // Act
            val result = client.timelineEvents("owner", "repo", 123)

            // Assert
            assertThat(result).hasSize(1)
            assertThat(result[0]).isInstanceOf(CommentedEvent::class.java)
            val commentedEvent = result[0] as CommentedEvent
            assertThat(commentedEvent.body).isEqualTo("This is a comment")
        }

    @Test
    fun `timelineEvents - parses unknown event type as UnknownEvent`() =
        runTest {
            // Arrange
            val timelineJson =
                """
                [{
                    "event": "some_unknown_event_type",
                    "created_at": "2023-01-01T18:00:00Z"
                }]
                """.trimIndent()

            val mockCache = mockk<DatabaseCacheService>()
            coEvery { mockCache.getCachedResponse(any()) } returns timelineJson

            val client = GhCliApiClient(databaseCacheService = mockCache)

            // Act
            val result = client.timelineEvents("owner", "repo", 123)

            // Assert
            assertThat(result).hasSize(1)
            assertThat(result[0]).isInstanceOf(UnknownEvent::class.java)
        }

    @Test
    fun `timelineEvents - parses mixed event types correctly`() =
        runTest {
            // Arrange
            val timelineJson =
                """
                [
                    {
                        "id": 123462,
                        "event": "reviewed",
                        "user": {
                            "login": "reviewer1",
                            "id": 9,
                            "avatar_url": "https://avatars.githubusercontent.com/u/9",
                            "url": "https://api.github.com/users/reviewer1",
                            "html_url": "https://github.com/reviewer1",
                            "type": "User"
                        },
                        "state": "approved",
                        "submitted_at": "2023-01-01T19:00:00Z",
                        "html_url": "https://github.com/owner/repo/pull/123#pullrequestreview-123462"
                    },
                    {
                        "id": 123463,
                        "event": "merged",
                        "url": "https://api.github.com/repos/owner/repo/issues/events/123463",
                        "actor": {
                            "login": "merger",
                            "id": 10,
                            "avatar_url": "https://avatars.githubusercontent.com/u/10",
                            "url": "https://api.github.com/users/merger",
                            "html_url": "https://github.com/merger",
                            "type": "User"
                        },
                        "commit_id": "def456",
                        "commit_url": "https://github.com/owner/repo/commit/def456",
                        "created_at": "2023-01-01T20:00:00Z"
                    },
                    {
                        "event": "unknown_type",
                        "created_at": "2023-01-01T21:00:00Z"
                    }
                ]
                """.trimIndent()

            val mockCache = mockk<DatabaseCacheService>()
            coEvery { mockCache.getCachedResponse(any()) } returns timelineJson

            val client = GhCliApiClient(databaseCacheService = mockCache)

            // Act
            val result = client.timelineEvents("owner", "repo", 123)

            // Assert
            assertThat(result).hasSize(3)
            assertThat(result[0]).isInstanceOf(ReviewedEvent::class.java)
            assertThat(result[1]).isInstanceOf(MergedEvent::class.java)
            assertThat(result[2]).isInstanceOf(UnknownEvent::class.java)
        }

    @Test
    fun `prSourceCodeReviewComments - parses review comments correctly`() =
        runTest {
            // Arrange
            val commentsJson =
                """
                [{
                    "id": 1001,
                    "user": {
                        "login": "code_reviewer",
                        "id": 11,
                        "avatar_url": "https://avatars.githubusercontent.com/u/11",
                        "url": "https://api.github.com/users/code_reviewer",
                        "html_url": "https://github.com/code_reviewer",
                        "type": "User"
                    },
                    "body": "Please fix this",
                    "created_at": "2023-01-01T22:00:00Z",
                    "updated_at": "2023-01-01T22:00:00Z",
                    "pull_request_url": "https://api.github.com/repos/owner/repo/pulls/123",
                    "html_url": "https://github.com/owner/repo/pull/123#discussion_r1001",
                    "pull_request_review_id": "2001",
                    "commit_id": "abc123def",
                    "original_commit_id": "abc123def",
                    "author_association": "CONTRIBUTOR"
                }]
                """.trimIndent()

            val mockCache = mockk<DatabaseCacheService>()
            coEvery { mockCache.getCachedResponse(any()) } returns commentsJson

            val client = GhCliApiClient(databaseCacheService = mockCache)

            // Act
            val result = client.prSourceCodeReviewComments("owner", "repo", 123)

            // Assert
            assertThat(result).hasSize(1)
            assertThat(result[0].id).isEqualTo(1001)
            assertThat(result[0].body).isEqualTo("Please fix this")
            assertThat(result[0].user.login).isEqualTo("code_reviewer")
        }

    @Test
    fun `searchIssues - parses search result with items correctly`() =
        runTest {
            // Arrange
            val searchJson =
                """
                {
                    "total_count": 1,
                    "incomplete_results": false,
                    "items": [{
                        "id": 2001,
                        "number": 100,
                        "title": "Test Issue",
                        "url": "https://api.github.com/repos/owner/repo/issues/100",
                        "html_url": "https://github.com/owner/repo/issues/100",
                        "user": {
                            "login": "issue_creator",
                            "id": 10,
                            "avatar_url": "https://avatars.githubusercontent.com/u/10",
                            "url": "https://api.github.com/users/issue_creator",
                            "html_url": "https://github.com/issue_creator",
                            "type": "User"
                        },
                        "state": "open",
                        "created_at": "2023-01-01T23:00:00Z",
                        "updated_at": "2023-01-01T23:00:00Z"
                    }]
                }
                """.trimIndent()

            val mockCache = mockk<DatabaseCacheService>()
            coEvery { mockCache.getCachedResponse(any()) } returns searchJson

            val client = GhCliApiClient(databaseCacheService = mockCache)

            // Act
            val result = client.searchIssues("test query")

            // Assert
            assertThat(result.total_count).isEqualTo(1)
            assertThat(result.items).hasSize(1)
            assertThat(result.items[0].number).isEqualTo(100)
            assertThat(result.items[0].title).isEqualTo("Test Issue")
        }

    @Test
    fun `topContributors - parses user list correctly`() =
        runTest {
            // Arrange
            val usersJson =
                """
                [{
                    "login": "contributor1",
                    "id": 11,
                    "avatar_url": "https://avatars.githubusercontent.com/u/11",
                    "url": "https://api.github.com/users/contributor1",
                    "html_url": "https://github.com/contributor1",
                    "type": "User",
                    "contributions": 100
                },
                {
                    "login": "contributor2",
                    "id": 12,
                    "avatar_url": "https://avatars.githubusercontent.com/u/12",
                    "url": "https://api.github.com/users/contributor2",
                    "html_url": "https://github.com/contributor2",
                    "type": "User",
                    "contributions": 50
                }]
                """.trimIndent()

            val mockCache = mockk<DatabaseCacheService>()
            coEvery { mockCache.getCachedResponse(any()) } returns usersJson

            val client = GhCliApiClient(databaseCacheService = mockCache)

            // Act
            val result = client.topContributors("owner", "repo")

            // Assert
            assertThat(result).hasSize(2)
            assertThat(result[0].login).isEqualTo("contributor1")
            assertThat(result[1].login).isEqualTo("contributor2")
        }

    @Test
    fun `pullRequests - parses PR list correctly`() =
        runTest {
            // Arrange
            val prsJson =
                """
                [{
                    "id": 3001,
                    "number": 201,
                    "state": "open",
                    "title": "First PR",
                    "url": "https://api.github.com/repos/owner/repo/pulls/201",
                    "html_url": "https://github.com/owner/repo/pull/201",
                    "user": {
                        "login": "pr_author1",
                        "id": 13,
                        "avatar_url": "https://avatars.githubusercontent.com/u/13",
                        "url": "https://api.github.com/users/pr_author1",
                        "html_url": "https://github.com/pr_author1",
                        "type": "User"
                    },
                    "merged": false,
                    "created_at": "2023-01-02T00:00:00Z",
                    "updated_at": "2023-01-02T00:00:00Z",
                    "closed_at": null,
                    "merged_at": null
                },
                {
                    "id": 3002,
                    "number": 202,
                    "state": "closed",
                    "title": "Second PR",
                    "url": "https://api.github.com/repos/owner/repo/pulls/202",
                    "html_url": "https://github.com/owner/repo/pull/202",
                    "user": {
                        "login": "pr_author2",
                        "id": 14,
                        "avatar_url": "https://avatars.githubusercontent.com/u/14",
                        "url": "https://api.github.com/users/pr_author2",
                        "html_url": "https://github.com/pr_author2",
                        "type": "User"
                    },
                    "merged": true,
                    "created_at": "2023-01-02T01:00:00Z",
                    "updated_at": "2023-01-02T02:00:00Z",
                    "closed_at": "2023-01-02T02:00:00Z",
                    "merged_at": "2023-01-02T02:00:00Z"
                }]
                """.trimIndent()

            val mockCache = mockk<DatabaseCacheService>()
            coEvery { mockCache.getCachedResponse(any()) } returns prsJson

            val client = GhCliApiClient(databaseCacheService = mockCache)

            // Act
            val result = client.pullRequests("owner", "repo")

            // Assert
            assertThat(result).hasSize(2)
            assertThat(result[0].number).isEqualTo(201)
            assertThat(result[0].state).isEqualTo("open")
            assertThat(result[1].number).isEqualTo(202)
            assertThat(result[1].merged).isTrue()
        }
}
