package com.sirwellington.target.common;

import java.sql.Connection;
import java.sql.Statement;

import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Loads and executes schema.sql to ensure tables exist. */
public final class SchemaMigration {

    private static final Logger logger = LoggerFactory.getLogger(SchemaMigration.class);

    private SchemaMigration() {}

    /** Reads schema.sql from the classpath and executes it against the given data source. */
    public static void run(HikariDataSource dataSource) {
        var schemaSql = Resources.load("/schema.sql");
        if (schemaSql == null || schemaSql.isBlank()) {
            logger.info("No schema.sql found, skipping migration.");
            return;
        }
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            try (Statement statement = connection.createStatement()) {
                statement.execute(schemaSql);
            }
        } catch (Exception e) {
            logger.error("Schema migration failed: {}", e.getMessage());
        }
        logger.info("Schema migration complete.");
    }
}
