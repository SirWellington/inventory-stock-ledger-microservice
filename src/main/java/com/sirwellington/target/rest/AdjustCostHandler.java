package com.sirwellington.target.rest;

import java.math.BigDecimal;

import com.sirwellington.target.db.InventoryRepository;
import com.sirwellington.target.model.TransactionType;
import com.sirwellington.target.producer.EventPublisher;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdjustCostHandler {

    private static final Logger LOG = LoggerFactory.getLogger(AdjustCostHandler.class);

    public record CostAdjustmentRequest(
        int quantityChange,
        BigDecimal unitCost,
        String reasonCode
    ) {}

    public record CostAdjustmentResponse(
        long transactionId,
        java.time.OffsetDateTime transactionTimestamp,
        String skuId,
        String transactionType,
        int quantityChange,
        BigDecimal unitCost,
        BigDecimal totalAmountImpact
    ) {}

    private final InventoryRepository repository;
    private final EventPublisher publisher;

    public AdjustCostHandler(InventoryRepository repository, EventPublisher publisher) {
        this.repository = repository;
        this.publisher = publisher;
    }

    public void handle(Context ctx) throws Exception {
        String skuId = ctx.pathParam("skuId");
        var request = ctx.bodyAsClass(CostAdjustmentRequest.class);

        var response = repository.insertTransaction(new InventoryRepository.InsertTransactionRequest(
            TransactionType.ADJUSTMENT,
            skuId,
            request.quantityChange(),
            request.unitCost()
        ));

        String payloadJson = String.format(
            "{\"transactionId\":%d,\"type\":\"%s\",\"skuId\":\"%s\",\"quantityChange\":%d,\"unitCost\":\"%s\"}",
            response.transactionId(),
            TransactionType.ADJUSTMENT.name(),
            skuId,
            request.quantityChange(),
            request.unitCost().toPlainString()
        );

        publisher.publish(skuId, payloadJson);

        LOG.info("Adjustment recorded: transactionId={}, sku={}", response.transactionId(), skuId);

        ctx.status(201).json(new CostAdjustmentResponse(
            response.transactionId(),
            response.transactionTimestamp(),
            skuId,
            TransactionType.ADJUSTMENT.name(),
            request.quantityChange(),
            request.unitCost(),
            BigDecimal.valueOf(request.quantityChange()).multiply(request.unitCost())
        ));
    }
}
