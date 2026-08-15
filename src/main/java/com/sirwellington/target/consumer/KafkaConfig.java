package com.sirwellington.target.consumer;

import java.util.Map;

import com.sirwellington.target.EnvConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

/**
 * Reads Kafka consumer settings from .env file, falling back to JVM system properties.
 */
public final class KafkaConfig {

    private KafkaConfig() {}

    public static final String BOOTSTRAP_SERVERS = EnvConfig.get("KAFKA_BOOTSTRAP", "localhost:9092");
    public static final String GROUP_ID           = EnvConfig.get("KAFKA_GROUP_ID", "inventory-consumer-group");
    public static final String TOPIC              = EnvConfig.get("KAFKA_TOPIC", "inventory-events");

    /** Creates a KafkaConsumer configured from {@link KafkaConfig}. */
    public static KafkaConsumer<String, String> createKafkaConsumer() {
        var props = Map.<String, Object>of(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaConfig.BOOTSTRAP_SERVERS,
            ConsumerConfig.GROUP_ID_CONFIG, KafkaConfig.GROUP_ID,
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName(),
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName(),
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
            ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true
        );
        return new KafkaConsumer<>(props);
    }
}
