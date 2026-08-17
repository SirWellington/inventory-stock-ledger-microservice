package com.sirwellington.target.rest;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Objects;

import javax.inject.Inject;

import com.sirwellington.target.db.InventoryRepository;
import com.sirwellington.target.model.EventPayload;
import com.sirwellington.target.model.TransactionType;
import com.sirwellington.target.producer.EventPublisher;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.sirwellington.alchemy.annotations.arguments.Required;

public class AdjustCostHandler {

    private static final Logger LOG = LoggerFactory.getLogger(AdjustCostHandler.class);

    public record CostAdjustmentRequest(
        int quantityChange,
        BigDecimal unitCost,
        String reasonCode
    ) {}

    public record CostAdjustmentResponse(
        long transactionId,
        OffsetDateTime transactionTimestamp,
        String skuId,
        String transactionType,
        int quantityChange,
        BigDecimal unitCost,
        BigDecimal totalAmountImpact
    ) {}

    private final InventoryRepository repository;
    private final EventPublisher publisher;

    @Inject
    public AdjustCostHandler(
        @Required InventoryRepository repository,
        @Required EventPublisher publisher
    ) {
        Objects.requireNonNull(repository);
        Objects.requireNonNull(publisher);
        this.repository = repository;
        this.publisher = publisher;
    }

    public void handle(Context ctx) throws Exception {
        var skuId = ctx.pathParam("skuId");
        var request = ctx.bodyAsClass(CostAdjustmentRequest.class);
        var response = repository.insertTransaction(new InventoryRepository.InsertTransactionRequest(
            TransactionType.ADJUSTMENT,
            skuId,
            request.quantityChange(),
            request.unitCost()
        ));

        var eventPayload = new EventPayload(
            response.transactionId(),
            TransactionType.ADJUSTMENT.name(),
            skuId,
            request.quantityChange(),
            request.unitCost()
        );

        publisher.publish(eventPayload);

        LOG.info(
            "Adjustment recorded and published: transactionId={}, sku={}",
            response.transactionId(),
            skuId
        );

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
