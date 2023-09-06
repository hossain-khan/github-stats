import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.asJdbcDriver
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import dev.hossain.githubstats.GitHubApiCacheDb
import dev.hossain.githubstats.HockeyPlayer
import dev.hossain.githubstats.PlayerQueries
import javax.sql.DataSource

fun main() {
    val dataSource: DataSource = getSource()
    val driver: SqlDriver = dataSource.asJdbcDriver()

    doDatabaseThings(driver)
}

private fun getSource(): DataSource {
    val config: HikariConfig = HikariConfig()
    // https://jdbc.postgresql.org/documentation/use/
    //config.setJdbcUrl( "jdbc:postgresql://192.168.2.32:5432/exampledb" );
    config.setJdbcUrl("jdbc:postgresql://192.168.2.32:5432/exampledb");
    // jdbc:postgresql:///<DATABASE_NAME>?cloudSqlInstance=<INSTANCE_CONNECTION_NAME>&socketFactory=com.google.cloud.sql.postgres.SocketFactory&user=<POSTGRESQL_USER_NAME>&password=<POSTGRESQL_USER_PASSWORD>
    //config.setJdbcUrl( "jdbc:postgresql:///exampledb?user=hkrpi&password=pgdb@pi");
    config.username = "hkrpi";
    config.password = "pgdb@pi";
    config.addDataSourceProperty( "cachePrepStmts" , "true" );
    config.addDataSourceProperty( "prepStmtCacheSize" , "250" );
    config.addDataSourceProperty( "prepStmtCacheSqlLimit" , "2048" );
    config.driverClassName = "org.postgresql.Driver"
    val ds = HikariDataSource( config );


    return ds.dataSource
}

fun doDatabaseThings(driver: SqlDriver) {
    val database = GitHubApiCacheDb(driver)
    val playerQueries: PlayerQueries = database.playerQueries

    println(playerQueries.selectAll().executeAsList())
    // [HockeyPlayer(15, "Ryan Getzlaf")]

    playerQueries.insert(player_number = 10, full_name = "Corey Perry")
    println(playerQueries.selectAll().executeAsList())
    // [HockeyPlayer(15, "Ryan Getzlaf"), HockeyPlayer(10, "Corey Perry")]

    val player = HockeyPlayer(10, "Ronald McDonald")
    playerQueries.insertFullPlayerObject(player)
}