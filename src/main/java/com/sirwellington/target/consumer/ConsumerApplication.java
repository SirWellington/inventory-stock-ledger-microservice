package com.sirwellington.target.consumer;

import java.sql.SQLException;
import java.time.Duration;
import java.util.Collections;

import com.sirwellington.target.db.DatabaseConfig;
import com.sirwellington.target.db.SchemaMigration;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Kafka consumer entry point. Subscribes to the inventory-events topic and
 * polls for messages in an infinite loop, printing received events to stdout.
 */
public class ConsumerApplication {

    private static final Logger LOG = LoggerFactory.getLogger(ConsumerApplication.class);

    /** Starts the Kafka consumer and begins polling for events. */
    public static void run() {
        var database = DatabaseConfig.createDataSource();
        var consumer = KafkaConfig.createKafkaConsumer();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOG.info("Shutting down...");
            consumer.close();
            database.close();
        }));
        try {
            SchemaMigration.run(database);
        } catch (SQLException ex) {
            LOG.error("Schema migration failed", ex);
            throw new RuntimeException("Schema migration failed", ex);
        }

        LOG.info("Subscribing to topic: {} at {}", KafkaConfig.TOPIC, KafkaConfig.BOOTSTRAP_SERVERS);
        consumer.subscribe(Collections.singletonList(KafkaConfig.TOPIC));
        LOG.info("Subscribed to topic: {}", KafkaConfig.TOPIC);
        pollLoop(consumer);
    }

    /** Infinite poll loop that prints received Kafka events to stdout. */
    private static void pollLoop(KafkaConsumer<String, String> consumer) {
        while (!Thread.currentThread().isInterrupted()) {
            var records = consumer.poll(Duration.ofMillis(5000));
            for (var record : records) {
                LOG.debug(
                    "Received event: topic={} partition={} offset={} key={} value={}",
                    record.topic(),
                    record.partition(),
                    record.offset(),
                    record.key(),
                    record.value()
                );
            }
        }
    }
}
