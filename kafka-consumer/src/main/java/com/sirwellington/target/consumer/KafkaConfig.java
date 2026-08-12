package com.sirwellington.target.consumer;

/**
 * Reads Kafka consumer settings from JVM system properties once at class load time.
 */
public final class KafkaConfig {

    private KafkaConfig() {}

    public static final String BOOTSTRAP_SERVERS = System.getProperty("kafka.bootstrap", "localhost:9092");
    public static final String GROUP_ID          = System.getProperty("kafka.group.id", "inventory-consumer-group");
    public static final String TOPIC             = System.getProperty("kafka.topic", "inventory-events");
}
