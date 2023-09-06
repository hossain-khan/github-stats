import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class PgDataSource {

    private static HikariConfig config = new HikariConfig();
    private static HikariDataSource ds;

    static {
        // https://jdbc.postgresql.org/documentation/use/
        //config.setJdbcUrl( "jdbc:postgresql://192.168.2.32:5432/exampledb" );
        config.setJdbcUrl( "jdbc:postgresql://192.168.2.32:5432/exampledb?user=hkrpi");
        config.setUsername( "hkrpi" );
        config.setPassword( "pgdb@pi" );
        config.addDataSourceProperty( "cachePrepStmts" , "true" );
        config.addDataSourceProperty( "prepStmtCacheSize" , "250" );
        config.addDataSourceProperty( "prepStmtCacheSqlLimit" , "2048" );
        ds = new HikariDataSource( config );
    }

    private PgDataSource() {}

    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

    public static javax.sql.DataSource getSource() {
        return ds.getDataSource();
    }
}