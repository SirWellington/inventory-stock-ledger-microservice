package com.sirwellington.target.db;

import com.sirwellington.target.EnvConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Reads PostgreSQL connection settings from .env file, falling back to JVM system properties.
 */
public final class DatabaseConfig {

    private DatabaseConfig() {}

    private static final String USERNAME = EnvConfig.get("DATABASE_USERNAME");
    private static final String PASSWORD = EnvConfig.get("DATABASE_PASSWORD");
    private static final String PORT     = EnvConfig.get("DATABASE_PORT", "5432");
    private static final String HOST     = EnvConfig.get("DATABASE_HOST", "localhost");
    private static final String NAME     = EnvConfig.get("DATABASE_NAME", "inventory_ledger_db");

    /** Builds and returns the full JDBC URL from host, port, and name. */
    public static String jdbcUrl() {
        return String.format("jdbc:postgresql://%s:%s/%s", HOST, PORT, NAME);
    }

    /** Creates a HikariCP connection pool for the application. */
    public static HikariDataSource createDataSource() {
        var config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl());
        config.setUsername(USERNAME);
        config.setPassword(PASSWORD);
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        return new HikariDataSource(config);
    }
}
