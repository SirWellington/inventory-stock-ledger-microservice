package com.sirwellington.target.rest;

import java.sql.SQLException;
import java.util.Map;

import com.sirwellington.target.db.DatabaseConfig;
import com.sirwellington.target.db.SchemaMigration;
import io.javalin.Javalin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * REST service entry point. Bootstraps Javalin, initializes the database pool,
 * and runs schema migration on startup.
 */
public class RestApplication {

    private static final Logger logger = LoggerFactory.getLogger(RestApplication.class);

    /** Starts the REST API server on port 7070. */
    public static void main() throws SQLException {
        var database = DatabaseConfig.createDataSource();
        SchemaMigration.run(database);

        var app = Javalin.create(config -> {
            config.routes.get("/health", ctx -> ctx.json(Map.of("status", "ok")));
        }).start(7070);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down...");
            database.close();
        }));

        logger.info("Listening on http://localhost:7070");
    }
}
