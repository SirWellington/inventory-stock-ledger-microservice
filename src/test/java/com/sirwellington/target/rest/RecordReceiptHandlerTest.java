package com.sirwellington.target.rest;

import java.math.BigDecimal;
import java.time.Instant;

import com.sirwellington.target.db.InventoryRepository.InsertTransactionResponse;
import com.sirwellington.target.rest.RecordReceiptHandler.RecordReceiptRequest;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import com.sirwellington.target.db.InventoryRepository;
import com.sirwellington.target.model.EventPayload;
import com.sirwellington.target.model.TransactionType;
import com.sirwellington.target.producer.EventPublisher;

import io.javalin.http.Context;
import tech.sirwellington.alchemy.test.AlchemyTest;
import tech.sirwellington.alchemy.test.ThrowableAssertion;
import tech.sirwellington.alchemy.test.generation.GenerateString;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static tech.sirwellington.alchemy.test.ThrowableAssertion.assertThrows;

@AlchemyTest
class RecordReceiptHandlerTest {

    @Mock
    private InventoryRepository repository;
    @Mock
    private EventPublisher publisher;
    @GenerateString
    private String skuId;

    private RecordReceiptHandler createHandler() {
        return new RecordReceiptHandler(repository, publisher);
    }

    @Test
    void testConstructorFailsWithNull() {
        assertThrows(() -> new RecordReceiptHandler(null, publisher));
        assertThrows(() -> new RecordReceiptHandler(repository, null));
        assertThrows(() -> new RecordReceiptHandler(null, null));
    }

    @Test
    void testRecordsReceiptSuccessfully() throws Exception {
        var request = new RecordReceiptRequest(
            skuId,
            100,
            BigDecimal.valueOf(5.50)
        );
        var now = Instant.now();
        var repositoryResponse = new InsertTransactionResponse(
            42L, now, BigDecimal.valueOf(5.50), BigDecimal.valueOf(550.00)
        );
        when(repository.insertTransaction(any()))
            .thenReturn(repositoryResponse);

        var ctx = mockContextWithBody(request);

        var handler = createHandler();
        handler.handle(ctx);

        var captor = ArgumentCaptor.forClass(EventPayload.class);
        verify(publisher).publish(captor.capture());
        assertThat(captor.getValue().transactionId()).isEqualTo(42L);
        assertThat(captor.getValue().type()).isEqualTo("RECEIPT");
        assertThat(captor.getValue().skuId()).isEqualTo(skuId);
        assertThat(captor.getValue().quantityChange()).isEqualTo(100);
        assertThat(captor.getValue().unitCost()).isEqualByComparingTo(BigDecimal.valueOf(5.50));

        verify(ctx).status(201);
    }

    @Test
    void testCalculatesCorrectTotalAmountImpact() throws Exception {
        var request = new RecordReceiptRequest(
            skuId,
            50,
            BigDecimal.valueOf(12.75)
        );
        var repositoryResponse = new InsertTransactionResponse(
            1L, Instant.now(), BigDecimal.valueOf(12.75), BigDecimal.valueOf(637.50)
        );
        when(repository.insertTransaction(any()))
            .thenReturn(repositoryResponse);

        var ctx = mockContextWithBody(request);
        var handler = createHandler();
        handler.handle(ctx);

        verify(repository).insertTransaction(any());
    }

    @Test
    void testRecordsReceiptWithDecimalUnitCost() throws Exception {
        var cost = new BigDecimal("99.9999");
        var request = new RecordReceiptRequest(skuId, 10, cost);
        var repositoryResponse = new InsertTransactionResponse(
            5L, Instant.now(), cost, new BigDecimal("1000.00")
        );
        when(repository.insertTransaction(any()))
            .thenReturn(repositoryResponse);

        var ctx = mockContextWithBody(request);
        var handler = createHandler();
        handler.handle(ctx);

        verify(ctx).status(201);

        var captor = ArgumentCaptor.forClass(RecordReceiptHandler.RecordReceiptResponse.class);
        verify(ctx).json(captor.capture());
        assertThat(captor.getValue().unitCost()).isEqualByComparingTo(cost);
        assertThat(captor.getValue().totalAmountImpact()).isEqualByComparingTo(new BigDecimal("1000.00"));
    }

    Context mockContextWithBody(Object body) {
        var ctx = mock(Context.class);
        when(ctx.bodyAsClass(any())).thenReturn(body);
        lenient().when(ctx.status(org.mockito.ArgumentMatchers.anyInt())).thenReturn(ctx);
        return ctx;
    }
}
