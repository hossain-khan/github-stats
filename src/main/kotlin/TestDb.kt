import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.asJdbcDriver
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import dev.hossain.githubstats.GitHubApiCacheDb
import dev.hossain.githubstats.HockeyPlayer
import dev.hossain.githubstats.PlayerQueries
import dev.hossain.githubstats.util.LocalProperties
import javax.sql.DataSource

fun main() {
    val newDataSource = getDataSource()
    val newDriver: SqlDriver = newDataSource.asJdbcDriver()

    doDatabaseThings(newDriver)
}

/**
 * Creates a [DataSource] using [HikariDataSource].
 */
private fun getDataSource(): DataSource {
    val properties = LocalProperties()
    val dbHost = properties.getProperty("db_host")
    val dbPort = properties.getProperty("db_port")
    val dbName = properties.getProperty("db_name")
    val userName = properties.getProperty("db_username")
    val dbPassword = properties.getProperty("db_password")

    // https://jdbc.postgresql.org/documentation/use/
    val config = HikariConfig().apply {
        jdbcUrl = "jdbc:postgresql://${dbHost}:${dbPort}/${dbName}"
        driverClassName = "org.postgresql.Driver"
        username = userName
        password = dbPassword
        maximumPoolSize = 3
        isAutoCommit = false
        transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        validate()
    }
    return HikariDataSource(config)
}

fun doDatabaseThings(driver: SqlDriver) {
    val database = GitHubApiCacheDb(driver)
    val responseCacheQueries = database.responseCacheQueries

    responseCacheQueries.selectAll()
    println(responseCacheQueries.selectAll().executeAsList())

}