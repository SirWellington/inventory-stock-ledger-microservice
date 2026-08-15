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

    private static final Logger LOG = LoggerFactory.getLogger(RestApplication.class);
    private static final int PORT = Integer.parseInt(System.getProperty("port", "7070"));

    /** Starts the REST API server. Port defaults to 7070; override with -Dport. */
    public static void main() throws SQLException {
        var database = DatabaseConfig.createDataSource();
        SchemaMigration.run(database);

        var app = Javalin.create(config -> {
            config.routes.get("/health", ctx -> ctx.json(Map.of("status", "ok")));
        });
        app.start(PORT);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOG.info("Shutting down...");
            database.close();
        }));

        LOG.info("Listening on http://localhost:{}", PORT);
    }
}
