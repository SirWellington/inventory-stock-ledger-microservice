package com.sirwellington.target;

import java.sql.Connection;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.sql.DataSource;

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

public class TargetModule extends AbstractModule {

    private final DataSource dataSource;

    @Inject
    public TargetModule(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Provides
    @Singleton
    ObjectMapper provideJsonMapper() {
        return new ObjectMapper();
    }

    @Provides
    @Singleton
    Connection provideConnection() throws Exception {
        return dataSource.getConnection();
    }

    @Provides
    @Singleton
    InventoryRepository provideInventoryRepository(Connection connection) {
        return new InventoryRepository(connection);
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

    @Provides
    @Singleton
    GetHealthHandler provideHealthHandler() {
        return new GetHealthHandler();
    }

    @Provides
    @Singleton
    RecordReceiptHandler provideReceiptHandler(InventoryRepository repository, EventPublisher publisher) {
        return new RecordReceiptHandler(repository, publisher);
    }

    @Provides
    @Singleton
    AdjustCostHandler provideCostAdjustmentHandler(InventoryRepository repository, EventPublisher publisher) {
        return new AdjustCostHandler(repository, publisher);
    }

    @Provides
    @Singleton
    GetCurrentValueHandler provideCurrentValueHandler(InventoryRepository repository) {
        return new GetCurrentValueHandler(repository);
    }

    @Provides
    @Singleton
    GetLedgerHistoryHandler provideLedgerHistoryHandler(InventoryRepository repository) {
        return new GetLedgerHistoryHandler(repository);
    }

}
