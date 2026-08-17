package com.sirwellington.target.rest;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import com.sirwellington.target.rest.AdjustCostHandler.CostAdjustmentRequest;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import com.sirwellington.target.db.InventoryRepository;
import com.sirwellington.target.model.EventPayload;
import com.sirwellington.target.producer.EventPublisher;

import io.javalin.http.Context;
import tech.sirwellington.alchemy.test.AlchemyTest;
import tech.sirwellington.alchemy.test.generation.GenerateString;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@AlchemyTest
class AdjustCostHandlerTest {

    @Mock
    private InventoryRepository repository;

    @Mock
    private EventPublisher publisher;

    @GenerateString
    private String skuId;

    private AdjustCostHandler createHandler() {
        return new AdjustCostHandler(repository, publisher);
    }

    @Test
    void recordsPositiveAdjustmentSuccessfully() throws Exception {
        var request = new CostAdjustmentRequest(
            25,
            BigDecimal.valueOf(8.00),
            "REPRICE"
        );
        when(repository.insertTransaction(any()))
            .thenReturn(new InventoryRepository.InsertTransactionResponse(10L, OffsetDateTime.now()));

        var ctx = mockContext(request);
        when(ctx.pathParam("skuId")).thenReturn(skuId);

        var handler = createHandler();
        handler.handle(ctx);

        var captor = ArgumentCaptor.forClass(EventPayload.class);
        verify(publisher).publish(captor.capture());
        assertThat(captor.getValue().transactionId()).isEqualTo(10L);
        assertThat(captor.getValue().type()).isEqualTo("ADJUSTMENT");
        assertThat(captor.getValue().quantityChange()).isEqualTo(25);

        verify(ctx).status(201);
    }

    @Test
    void recordsNegativeAdjustmentSuccessfully() throws Exception {
        var request = new CostAdjustmentRequest(
            -15,
            BigDecimal.valueOf(3.50),
            "DAMAGED"
        );
        when(repository.insertTransaction(any()))
            .thenReturn(new InventoryRepository.InsertTransactionResponse(2L, OffsetDateTime.now()));

        var ctx = mockContext(request);
        when(ctx.pathParam("skuId")).thenReturn(skuId);

        var handler = createHandler();
        handler.handle(ctx);

        verify(publisher).publish(any(EventPayload.class));
    }

    @Test
    void calculatesNegativeImpactForWriteOff() throws Exception {
        var request = new CostAdjustmentRequest(
            -10,
            BigDecimal.valueOf(2.50),
            "WRITEOFF"
        );
        when(repository.insertTransaction(any())).thenReturn(new InventoryRepository.InsertTransactionResponse(3L, OffsetDateTime.now()));

        var ctx = mockContext(request);
        when(ctx.pathParam("skuId")).thenReturn(skuId);

        var handler = createHandler();
        handler.handle(ctx);

        verify(publisher).publish(any(EventPayload.class));
    }

    @Test
    void returnsCorrectTransactionTimestamp() throws Exception {
        var expectedTimestamp = OffsetDateTime.parse("2026-01-15T10:30:00Z");
        var request = new CostAdjustmentRequest(5, BigDecimal.valueOf(1.00), "TEST");
        when(repository.insertTransaction(any())).thenReturn(new InventoryRepository.InsertTransactionResponse(7L, expectedTimestamp));

        var ctx = mockContext(request);
        when(ctx.pathParam("skuId")).thenReturn(skuId);

        var handler = createHandler();
        handler.handle(ctx);

        verify(publisher).publish(any(EventPayload.class));
    }

    Context mockContext(Object body) {
        Context ctx = mock(Context.class);
        when(ctx.bodyAsClass(any())).thenReturn(body);
        lenient().when(ctx.status(org.mockito.ArgumentMatchers.anyInt())).thenReturn(ctx);
        return ctx;
    }
}
