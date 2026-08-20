package dev.hossain.githubstats.cache

import app.cash.sqldelight.Query
import app.cash.sqldelight.Transacter
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlPreparedStatement
import app.cash.sqldelight.driver.jdbc.JdbcCursor
import com.google.common.truth.Truth.assertThat
import dev.hossain.githubstats.cache.database.postgres.PostgreSqlDatabase
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.sql.ResultSet
import java.time.OffsetDateTime

/**
 * Tests for [PostgreSqlDatabaseCacheService].
 */
class PostgreSqlDatabaseCacheServiceTest {
    private lateinit var fakeDriver: FakeSqlDriver
    private lateinit var database: PostgreSqlDatabase
    private lateinit var service: PostgreSqlDatabaseCacheService

    @BeforeEach
    fun setUp() {
        fakeDriver = FakeSqlDriver()
        database = PostgreSqlDatabase(fakeDriver)
        service = PostgreSqlDatabaseCacheService(database, expirationHours = 24L)
    }

    @Test
    fun `getCachedResponse returns cached data on hit`() =
        runBlocking {
            val url = "https://api.github.com/repos/testowner/testrepo/pulls/1"
            val cachedJson = """{"id": 1, "title": "Test PR"}"""

            val mockResultSet = mockk<ResultSet>(relaxed = true)
            every { mockResultSet.next() } returns true andThen false
            every { mockResultSet.getString(1) } returns "key1"
            every { mockResultSet.getString(2) } returns cachedJson
            every { mockResultSet.getObject(3, any<Class<*>>()) } returns OffsetDateTime.now()
            every { mockResultSet.getObject(4, any<Class<*>>()) } returns OffsetDateTime.now().plusHours(24)
            every { mockResultSet.getString(5) } returns url
            every { mockResultSet.getInt(6) } returns 200

            fakeDriver.cursorResult = JdbcCursor(mockResultSet)

            val result = service.getCachedResponse(url)
            assertThat(result).isEqualTo(cachedJson)
        }

    @Test
    fun `getCachedResponse returns null on miss`() =
        runBlocking {
            val url = "https://api.github.com/repos/testowner/testrepo/pulls/2"

            val mockResultSet = mockk<ResultSet>(relaxed = true)
            every { mockResultSet.next() } returns false

            fakeDriver.cursorResult = JdbcCursor(mockResultSet)

            val result = service.getCachedResponse(url)
            assertThat(result).isNull()
        }

    @Test
    fun `getCachedResponse returns null on exception`() =
        runBlocking {
            val url = "https://api.github.com/repos/testowner/testrepo/pulls/3"
            fakeDriver.shouldThrow = true

            val result = service.getCachedResponse(url)
            assertThat(result).isNull()
        }

    @Test
    fun `cacheResponse inserts response into database`() =
        runBlocking {
            val url = "https://api.github.com/repos/testowner/testrepo/pulls/4"
            val json = """{"id": 4}"""

            service.cacheResponse(url, json, 200)

            assertThat(fakeDriver.executedStatements).isNotEmpty()
        }

    @Test
    fun `cacheResponse handles exception gracefully`() =
        runBlocking {
            val url = "https://api.github.com/repos/testowner/testrepo/pulls/5"
            val json = """{"id": 5}"""
            fakeDriver.shouldThrow = true

            service.cacheResponse(url, json, 200)
        }

    @Test
    fun `cleanupExpiredEntries calls deleteExpiredResponses`() =
        runBlocking {
            service.cleanupExpiredEntries()

            assertThat(fakeDriver.executedStatements).isNotEmpty()
        }

    @Test
    fun `cleanupExpiredEntries handles exception gracefully`() =
        runBlocking {
            fakeDriver.shouldThrow = true

            service.cleanupExpiredEntries()
        }

    @Test
    fun `getCacheStats returns mapped stats when query succeeds`() =
        runBlocking {
            val mockResultSet = mockk<ResultSet>(relaxed = true)
            every { mockResultSet.next() } returns true andThen false
            every { mockResultSet.getLong(1) } returns 50L
            every { mockResultSet.getLong(2) } returns 45L
            every { mockResultSet.getLong(3) } returns 5L

            fakeDriver.cursorResult = JdbcCursor(mockResultSet)

            val stats = service.getCacheStats()
            assertThat(stats).isNotNull()
            assertThat(stats?.totalEntries).isEqualTo(50L)
            assertThat(stats?.validEntries).isEqualTo(45L)
            assertThat(stats?.expiredEntries).isEqualTo(5L)
        }

    @Test
    fun `getCacheStats returns null on exception`() =
        runBlocking {
            fakeDriver.shouldThrow = true

            val stats = service.getCacheStats()
            assertThat(stats).isNull()
        }

    private class FakeSqlDriver : SqlDriver {
        val executedStatements = mutableListOf<String>()
        var cursorResult: SqlCursor? = null
        var shouldThrow: Boolean = false

        override fun execute(
            identifier: Int?,
            sql: String,
            parameters: Int,
            binders: (SqlPreparedStatement.() -> Unit)?,
        ): QueryResult<Long> {
            if (shouldThrow) throw RuntimeException("DB error")
            executedStatements.add(sql)
            return QueryResult.Value(1L)
        }

        override fun <R> executeQuery(
            identifier: Int?,
            sql: String,
            mapper: (SqlCursor) -> QueryResult<R>,
            parameters: Int,
            binders: (SqlPreparedStatement.() -> Unit)?,
        ): QueryResult<R> {
            if (shouldThrow) throw RuntimeException("DB error")
            val cursor = cursorResult ?: mockk(relaxed = true)
            return mapper(cursor)
        }

        override fun newTransaction(): QueryResult<Transacter.Transaction> = QueryResult.Value(mockk(relaxed = true))

        override fun currentTransaction(): Transacter.Transaction? = null

        override fun close() {}

        override fun addListener(
            vararg queryKeys: String,
            listener: Query.Listener,
        ) {}

        override fun removeListener(
            vararg queryKeys: String,
            listener: Query.Listener,
        ) {}

        override fun notifyListeners(vararg queryKeys: String) {}
    }
}
