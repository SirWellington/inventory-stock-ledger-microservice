package com.sirwellington.target.consumer;

import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

/**
 * Reads Kafka consumer settings from JVM system properties once at class load time.
 */
public final class KafkaConfig {

    private KafkaConfig() {}

    private static final String BOOTSTRAP_SERVERS = System.getProperty("kafka.bootstrap", "localhost:9092");
    public static final String GROUP_ID           = System.getProperty("kafka.group.id", "inventory-consumer-group");
    public static final String TOPIC              = System.getProperty("kafka.topic", "inventory-events");

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
