package com.sirwellington.target.rest;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import com.sirwellington.target.db.InventoryRepository;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetLedgerHistoryHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GetLedgerHistoryHandler.class);

    public record LedgerEntry(
        long transactionId,
        OffsetDateTime transactionTimestamp,
        String skuId,
        String transactionType,
        int quantityChange,
        java.math.BigDecimal unitCost,
        java.math.BigDecimal totalAmountImpact
    ) {}

    public record LedgerHistoryResponse(
        List<LedgerEntry> entries
    ) {}

    private final InventoryRepository repository;

    public GetLedgerHistoryHandler(InventoryRepository repository) {
        this.repository = repository;
    }

    public void handle(Context ctx) throws Exception {
        String startDateStr = ctx.queryParam("startDate");
        String endDateStr = ctx.queryParam("endDate");

        if (startDateStr == null || endDateStr == null) {
            ctx.status(400).json(Map.of(
                "error", "Both 'startDate' and 'endDate' query parameters are required"
            ));
            return;
        }

        OffsetDateTime startDate = OffsetDateTime.parse(startDateStr);
        OffsetDateTime endDate = OffsetDateTime.parse(endDateStr);

        var result = repository.getLedgerHistory(new InventoryRepository.GetLedgerHistoryQuery(
            startDate,
            endDate
        ));

        List<LedgerEntry> entries = result.transactions().stream()
            .map(t -> new LedgerEntry(
                t.transactionId(),
                t.transactionTimestamp(),
                t.skuId(),
                t.transactionType(),
                t.quantityChange(),
                t.unitCost(),
                t.totalAmountImpact()
            ))
            .toList();

        LOG.info("Ledger history query: start={}, end={}, count={}",
            startDate, endDate, entries.size());

        ctx.json(new LedgerHistoryResponse(entries));
    }
}
