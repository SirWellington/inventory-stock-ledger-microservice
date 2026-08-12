package com.sirwellington.target.rest;

/**
 * Reads PostgreSQL connection settings from JVM system properties once at class load time.
 */
public final class DatabaseConfig {

    private DatabaseConfig() {}

    public static final String USERNAME = System.getProperty("database.username");
    public static final String PASSWORD = System.getProperty("database.password");
    public static final String PORT     = System.getProperty("database.port", "5432");
    public static final String HOST     = System.getProperty("database.host", "localhost");
    public static final String NAME     = System.getProperty("database.name", "inventory_ledger_db");

    /** Builds and returns the full JDBC URL from host, port, and name. */
    public static String jdbcUrl() {
        return String.format("jdbc:postgresql://%s:%s/%s", HOST, PORT, NAME);
    }
}
