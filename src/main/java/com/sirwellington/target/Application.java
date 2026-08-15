package com.sirwellington.target;

import com.sirwellington.target.consumer.ConsumerApplication;
import com.sirwellington.target.rest.RestApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unified entry point. Dispatches consumer and REST server on separate threads
 * based on CLI arguments: "consumer", "rest", or "all".
 */
public class Application {

    private static final Logger LOG = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        var mode = args.length > 0 ? args[0] : "all";

        switch (mode.toLowerCase()) {
            case "consumer" -> runConsumer();
            case "rest" -> runRest();
            case "all" -> runAll();
            default -> {
                LOG.warn("Unknown mode '{}', running both services.", mode);
                runAll();
            }
        }
    }

    private static void runConsumer() {
        var thread = Thread.startVirtualThread(ConsumerApplication::run);
        LOG.info("Running consumer service on dedicated thread.");
        try {
            thread.join();
        } catch (InterruptedException ex) {
            LOG.warn("Consumer interrupted", ex);
            Thread.currentThread().interrupt();
        }
    }

    private static void runRest() {
        var thread = Thread.startVirtualThread(RestApplication::run);
        LOG.info("Running REST service on dedicated thread.");
        try {
            thread.join();
        } catch (InterruptedException ex) {
            LOG.warn("REST server interrupted", ex);
            Thread.currentThread().interrupt();
        }
    }

    private static void runAll() {
        var consumerThread = Thread.startVirtualThread(ConsumerApplication::run);
        var restThread = Thread.startVirtualThread(RestApplication::run);

        LOG.info("Running both consumer and REST services on separate threads.");

        try {
            consumerThread.join();
            restThread.join();
        } catch (InterruptedException ex) {
            LOG.warn("Application interrupted", ex);
            Thread.currentThread().interrupt();
        }
    }
}
