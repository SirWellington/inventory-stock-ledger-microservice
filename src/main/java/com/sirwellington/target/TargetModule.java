package com.sirwellington.target;

import java.sql.Connection;
import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.sql.DataSource;
import javax.xml.crypto.Data;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.sirwellington.target.db.InventoryRepository;
import com.sirwellington.target.producer.EventPublisher;
import com.sirwellington.target.producer.KafkaProducerConfig;
import com.sirwellington.target.rest.AdjustCostHandler;
import com.sirwellington.target.rest.GetCurrentValueHandler;
import com.sirwellington.target.rest.GetHealthHandler;
import com.sirwellington.target.rest.GetLedgerHistoryHandler;
import com.sirwellington.target.rest.RecordReceiptHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.KafkaProducer;
import tech.sirwellington.alchemy.annotations.arguments.Required;

public class TargetModule extends AbstractModule {

    private final DataSource dataSource;

    @Inject
    public TargetModule(@Required DataSource dataSource) {
        Objects.requireNonNull(dataSource);
        this.dataSource = dataSource;
    }

    @Provides
    @Singleton
    ObjectMapper provideJsonMapper() {
        return new ObjectMapper()
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            .enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Provides
    DataSource provideDataSource() {
        return dataSource;
    }

    @Provides
    @Singleton
    InventoryRepository provideInventoryRepository(DataSource connection) {
        return new InventoryRepository(dataSource);
    }

    @Provides
    @Singleton
    KafkaProducer<String, String> provideKafkaProducer() {
        return KafkaProducerConfig.create();
    }

    @Provides
    @Singleton
    EventPublisher provideEventPublisher(KafkaProducer<String, String> producer, ObjectMapper objectMapper) {
        return new EventPublisher(producer, objectMapper);
    }

}
