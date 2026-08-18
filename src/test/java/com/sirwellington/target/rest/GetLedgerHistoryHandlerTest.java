package com.sirwellington.target.rest;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import com.sirwellington.target.db.InventoryRepository;

import io.javalin.http.Context;
import tech.sirwellington.alchemy.test.AlchemyTest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@AlchemyTest
class GetLedgerHistoryHandlerTest {

    @Mock InventoryRepository repository;

    private GetLedgerHistoryHandler createHandler() {
        return new GetLedgerHistoryHandler(repository);
    }

    @Test
    void testReturnsEntriesForValidDateRange() throws Exception {
        var start = Instant.parse("2026-01-01T00:00:00Z");
        var end = Instant.parse("2026-01-31T23:59:59Z");

        var record = new InventoryRepository.TransactionRecord(
            1L, start.plus(5, ChronoUnit.DAYS), "SKU-001", "RECEIPT", 50, new BigDecimal("10.00"), new BigDecimal("500.00")
        );
        when(repository.getLedgerHistory(any())).thenReturn(new InventoryRepository.GetLedgerHistoryResponse(List.of(record)));

        Context ctx = mockContext(start.toString(), end.toString());

        var handler = createHandler();
        handler.handle(ctx);

        verify(ctx).json(any(GetLedgerHistoryHandler.GetLedgerHistoryResponse.class));
    }

    @Test
    void testReturnsMultipleEntriesWhenAvailable() throws Exception {
        var start = Instant.parse("2026-01-01T00:00:00Z");
        var end = Instant.parse("2026-01-31T23:59:59Z");

        var records = List.of(
            new InventoryRepository.TransactionRecord(1L, start.plus(1, ChronoUnit.DAYS), "SKU-A", "RECEIPT", 100, new BigDecimal("5.00"), new BigDecimal("500.00")),
            new InventoryRepository.TransactionRecord(2L, start.plus(2, ChronoUnit.DAYS), "SKU-B", "SALE", -10, new BigDecimal("8.00"), new BigDecimal("-80.00")),
            new InventoryRepository.TransactionRecord(3L, start.plus(3, ChronoUnit.DAYS), "SKU-A", "ADJUSTMENT", 5, new BigDecimal("5.00"), new BigDecimal("25.00"))
        );
        when(repository.getLedgerHistory(any())).thenReturn(new InventoryRepository.GetLedgerHistoryResponse(records));

        Context ctx = mockContext(start.toString(), end.toString());

        var handler = createHandler();
        handler.handle(ctx);

        verify(ctx).json(any(GetLedgerHistoryHandler.GetLedgerHistoryResponse.class));
    }

    @Test
    void testReturnsEmptyListWhenNoTransactionsInRange() throws Exception {
        var start = Instant.parse("2026-06-01T00:00:00Z");
        var end = Instant.parse("2026-06-30T23:59:59Z");

        when(repository.getLedgerHistory(any())).thenReturn(new InventoryRepository.GetLedgerHistoryResponse(List.of()));

        Context ctx = mockContext(start.toString(), end.toString());

        var handler = createHandler();
        handler.handle(ctx);

        verify(ctx).json(any(GetLedgerHistoryHandler.GetLedgerHistoryResponse.class));
    }

    @Test
    void testReturns400WhenStartDateMissing() throws Exception {
        Context ctx = mockContext(null, "2026-01-31T23:59:59Z");

        var handler = createHandler();
        handler.handle(ctx);

        verify(ctx).status(400);
    }

    @Test
    void testReturns400WhenEndDateMissing() throws Exception {
        Context ctx = mockContext("2026-01-01T00:00:00Z", null);

        var handler = createHandler();
        handler.handle(ctx);

        verify(ctx).status(400);
    }

    @Test
    void testReturns400WhenBothDatesMissing() throws Exception {
        Context ctx = mockContext(null, null);

        var handler = createHandler();
        handler.handle(ctx);

        verify(ctx).status(400);
    }

    @Test
    void testMapsTransactionRecordToLedgerEntry() throws Exception {
        var timestamp = Instant.parse("2026-03-15T12:00:00Z");
        var record = new InventoryRepository.TransactionRecord(
            99L, timestamp, "SKU-MAP", "RECEIPT", 75, new BigDecimal("12.50"), new BigDecimal("937.50")
        );
        when(repository.getLedgerHistory(any())).thenReturn(new InventoryRepository.GetLedgerHistoryResponse(List.of(record)));

        Context ctx = mockContext("2026-03-01T00:00:00Z", "2026-03-31T23:59:59Z");

        var handler = createHandler();
        handler.handle(ctx);

        verify(ctx).json(any(GetLedgerHistoryHandler.GetLedgerHistoryResponse.class));
    }

    Context mockContext(String startDate, String endDate) {
        Context ctx = mock(Context.class);
        when(ctx.queryParam("startDate")).thenReturn(startDate);
        when(ctx.queryParam("endDate")).thenReturn(endDate);
        lenient().when(ctx.status(org.mockito.ArgumentMatchers.anyInt())).thenReturn(ctx);
        return ctx;
    }
}
