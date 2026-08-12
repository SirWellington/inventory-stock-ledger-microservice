package com.sirwellington.target.consumer;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Reads PostgreSQL connection settings from JVM system properties once at class load time.
 */
public final class DatabaseConfig {

    private DatabaseConfig() {}

    private static final String USERNAME = System.getProperty("database.username");
    private static final String PASSWORD = System.getProperty("database.password");
    private static final String PORT     = System.getProperty("database.port", "5432");
    private static final String HOST     = System.getProperty("database.host", "localhost");
    private static final String NAME     = System.getProperty("database.name", "inventory_ledger_db");

    /** Builds and returns the full JDBC URL from host, port, and name. */
    public static String jdbcUrl() {
        return String.format("jdbc:postgresql://%s:%s/%s", HOST, PORT, NAME);
    }

    /** Creates a HikariCP connection pool from {@link DatabaseConfig}. */
    public static HikariDataSource createDataSource() {
        var config = new HikariConfig();
        config.setJdbcUrl(DatabaseConfig.jdbcUrl());
        config.setUsername(DatabaseConfig.USERNAME);
        config.setPassword(DatabaseConfig.PASSWORD);
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        return new HikariDataSource(config);
    }
}
