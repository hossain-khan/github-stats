package dev.hossain.githubstats.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.net.URLEncoder
import kotlin.text.Charsets

class SearchParamsTest {

    private fun String.encode(): String = URLEncoder.encode(this, Charsets.UTF_8.name())

    @Test
    fun `toQuery with all parameters provides correctly formatted and encoded query`() {
        val params = SearchParams(
            repoOwner = "test-owner",
            repoId = "test-repo",
            author = "test-author",
            reviewer = "test-reviewer",
            dateAfter = "2023-01-01",
            dateBefore = "2023-01-31"
        )
        val expectedQuery =
            "${"is:closed".encode()}+" +
            "${"is:pr".encode()}+" +
            "${"is:merged".encode()}+" +
            "${"repo:test-owner/test-repo".encode()}+" +
            "${"created:2023-01-01..2023-01-31".encode()}+" +
            "${"author:test-author".encode()}+" +
            "reviewed-by:test-reviewer".encode()

        assertEquals(expectedQuery, params.toQuery())
    }

    @Test
    fun `toQuery with only required parameters excludes author and reviewer`() {
        val params = SearchParams(
            repoOwner = "owner2",
            repoId = "repo2",
            dateAfter = "2023-02-01",
            dateBefore = "2023-02-28",
            author = null,
            reviewer = null
        )
        val expectedQuery =
            "${"is:closed".encode()}+" +
            "${"is:pr".encode()}+" +
            "${"is:merged".encode()}+" +
            "${"repo:owner2/repo2".encode()}+" +
            "created:2023-02-01..2023-02-28".encode()

        assertEquals(expectedQuery, params.toQuery())
    }

    @Test
    fun `toQuery with author but no reviewer includes author and excludes reviewer`() {
        val params = SearchParams(
            repoOwner = "owner3",
            repoId = "repo3",
            author = "author3",
            dateAfter = "2023-03-01",
            dateBefore = "2023-03-31",
            reviewer = null
        )
        val expectedQuery =
            "${"is:closed".encode()}+" +
            "${"is:pr".encode()}+" +
            "${"is:merged".encode()}+" +
            "${"repo:owner3/repo3".encode()}+" +
            "${"created:2023-03-01..2023-03-31".encode()}+" +
            "author:author3".encode()

        assertEquals(expectedQuery, params.toQuery())
    }

    @Test
    fun `toQuery with reviewer but no author includes reviewer and excludes author`() {
        val params = SearchParams(
            repoOwner = "owner4",
            repoId = "repo4",
            reviewer = "reviewer4",
            dateAfter = "2023-04-01",
            dateBefore = "2023-04-30",
            author = null
        )
        val expectedQuery =
            "${"is:closed".encode()}+" +
            "${"is:pr".encode()}+" +
            "${"is:merged".encode()}+" +
            "${"repo:owner4/repo4".encode()}+" +
            "${"created:2023-04-01..2023-04-30".encode()}+" +
            "reviewed-by:reviewer4".encode()

        assertEquals(expectedQuery, params.toQuery())
    }

    @Test
    fun `toQuery ensures date range is correctly formatted`() {
        val params = SearchParams(
            repoOwner = "owner5",
            repoId = "repo5",
            dateAfter = "2022-12-01",
            dateBefore = "2022-12-31"
        )
        // Extracting the date part for specific check, though other tests also cover it implicitly
        val query = params.toQuery()
        val expectedDatePart = "created:2022-12-01..2022-12-31".encode()
        kotlin.test.assertTrue(query.contains(expectedDatePart), "Query should contain encoded date range '$expectedDatePart', but was '$query'")
    }

    @Test
    fun `toQuery ensures all parts are URL encoded`() {
        // This test re-verifies encoding using known values, similar to the first test,
        // but focuses on the encoding aspect itself.
        // Using a repo name with special characters that need encoding.
        val params = SearchParams(
            repoOwner = "test-owner/withslash",
            repoId = "test-repo&id",
            author = "test author", // Contains space
            reviewer = "test/reviewer", // Contains slash
            dateAfter = "2023-01-01",
            dateBefore = "2023-01-31"
        )

        // Manually construct the expected encoded string for comparison
        val expectedRepo = "repo:test-owner/withslash/test-repo&id".encode()
        val expectedAuthor = "author:test author".encode()
        val expectedReviewer = "reviewed-by:test/reviewer".encode()
        val expectedCreated = "created:2023-01-01..2023-01-31".encode()

        val actualQuery = params.toQuery()

        kotlin.test.assertTrue(actualQuery.contains(expectedRepo), "Query missing or incorrect repo encoding. Expected to contain: $expectedRepo")
        kotlin.test.assertTrue(actualQuery.contains(expectedAuthor), "Query missing or incorrect author encoding. Expected to contain: $expectedAuthor")
        kotlin.test.assertTrue(actualQuery.contains(expectedReviewer), "Query missing or incorrect reviewer encoding. Expected to contain: $expectedReviewer")
        kotlin.test.assertTrue(actualQuery.contains(expectedCreated), "Query missing or incorrect date encoding. Expected to contain: $expectedCreated")

        // Also check the static parts
        kotlin.test.assertTrue(actualQuery.contains("is%3Aclosed".encode()), "Query missing encoded 'is:closed'")
        kotlin.test.assertTrue(actualQuery.contains("is%3Apr".encode()), "Query missing encoded 'is:pr'")
        kotlin.test.assertTrue(actualQuery.contains("is%3Amerged".encode()), "Query missing encoded 'is:merged'")
    }
}
