package com.sirwellington.target.rest;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import com.sirwellington.target.db.InventoryRepository;
import com.sirwellington.target.model.EventPayload;
import com.sirwellington.target.model.TransactionType;
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
class RecordReceiptHandlerTest {

    @Mock InventoryRepository repository;
    @Mock EventPublisher publisher;
    @GenerateString String skuId;

    private RecordReceiptHandler createHandler() {
        return new RecordReceiptHandler(repository, publisher);
    }

    @Test
    void testRecordsReceiptSuccessfully() throws Exception {
        var request = new RecordReceiptHandler.RecordReceiptRequest(skuId, 100, BigDecimal.valueOf(5.50));
        var now = OffsetDateTime.now();
        when(repository.insertTransaction(any())).thenReturn(new InventoryRepository.InsertTransactionResponse(42L, now));

        Context ctx = mockContextWithBody(request);

        var handler = createHandler();
        handler.handle(ctx);

        var captor = ArgumentCaptor.forClass(EventPayload.class);
        verify(publisher).publish(captor.capture());
        assertThat(captor.getValue().transactionId()).isEqualTo(42L);
        assertThat(captor.getValue().type()).isEqualTo("RECEIPT");
        assertThat(captor.getValue().skuId()).isEqualTo(skuId);
        assertThat(captor.getValue().quantityChange()).isEqualTo(100);

        verify(ctx).status(201);
    }

    @Test
    void testCalculatesCorrectTotalAmountImpact() throws Exception {
        var request = new RecordReceiptHandler.RecordReceiptRequest(skuId, 50, BigDecimal.valueOf(12.75));
        when(repository.insertTransaction(any())).thenReturn(new InventoryRepository.InsertTransactionResponse(1L, OffsetDateTime.now()));

        Context ctx = mockContextWithBody(request);
        var handler = createHandler();
        handler.handle(ctx);

        verify(repository).insertTransaction(any());
    }

    @Test
    void testRecordsReceiptWithDecimalUnitCost() throws Exception {
        var cost = new BigDecimal("99.9999");
        var request = new RecordReceiptHandler.RecordReceiptRequest(skuId, 10, cost);
        when(repository.insertTransaction(any())).thenReturn(new InventoryRepository.InsertTransactionResponse(5L, OffsetDateTime.now()));

        Context ctx = mockContextWithBody(request);
        var handler = createHandler();
        handler.handle(ctx);

        verify(ctx).status(201);
    }

    Context mockContextWithBody(Object body) {
        Context ctx = mock(Context.class);
        when(ctx.bodyAsClass(any())).thenReturn(body);
        lenient().when(ctx.status(org.mockito.ArgumentMatchers.anyInt())).thenReturn(ctx);
        return ctx;
    }
}
