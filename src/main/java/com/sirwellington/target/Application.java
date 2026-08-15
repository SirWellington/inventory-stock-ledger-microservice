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
        var thread = new Thread(ConsumerApplication::run, "consumer-thread");
        thread.setDaemon(false);
        thread.start();
        LOG.info("Running consumer service on dedicated thread.");
    }

    private static void runRest() {
        var thread = new Thread(RestApplication::run, "rest-thread");
        thread.setDaemon(false);
        thread.start();
        LOG.info("Running REST service on dedicated thread.");
    }

    private static void runAll() {
        var consumerThread = new Thread(ConsumerApplication::run, "consumer-thread");
        consumerThread.setDaemon(false);
        consumerThread.start();

        var restThread = new Thread(RestApplication::run, "rest-thread");
        restThread.setDaemon(false);
        restThread.start();

        LOG.info("Running both consumer and REST services on separate threads.");

        try {
            consumerThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
