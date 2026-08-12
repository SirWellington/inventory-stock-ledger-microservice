package com.sirwellington.target.consumer;

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

    private static final Logger logger = LoggerFactory.getLogger(ConsumerApplication.class);

    /** Starts the Kafka consumer and begins polling for events. */
    public static void main(String[] args) {
        var dataSource = DatabaseConfig.createDataSource();
        SchemaMigration.run(dataSource);

        var consumer = KafkaConfig.createKafkaConsumer();
        consumer.subscribe(Collections.singletonList(KafkaConfig.TOPIC));

        logger.info("Subscribed to topic: {}", KafkaConfig.TOPIC);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down...");
            consumer.close();
            dataSource.close();
        }));

        pollLoop(consumer);
    }

    /** Infinite poll loop that prints received Kafka events to stdout. */
    private static void pollLoop(KafkaConsumer<String, String> consumer) {
        while (!Thread.currentThread().isInterrupted()) {
            var records = consumer.poll(Duration.ofMillis(1000));
            for (var record : records) {
                logger.debug("Received event: topic={} partition={} offset={} key={} value={}",
                        record.topic(), record.partition(), record.offset(), record.key(), record.value());
            }
        }
    }
}
