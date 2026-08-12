package com.sirwellington.target.rest;

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
    public static void main(String[] args) {
        var dataSource = DatabaseConfig.createDataSource();
        SchemaMigration.run(dataSource);

        var app = Javalin.create(config -> {
            config.routes.get("/health", ctx -> ctx.json(Map.of("status", "ok")));
        }).start(7070);

        logger.info("Listening on http://localhost:7070");
    }
}
