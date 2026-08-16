package com.sirwellington.target.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public enum Model {
    ;

    public enum TransactionType {
        ADJUSTMENT,
        RECEIPT,
        SALE
    }

    public record AdjustmentRequest(
        int quantityChange,
        BigDecimal unitCost,
        String reasonCode
    ) {}

    public record GetInventoryValueResponse(
        String skuId,
        int currentQuantity,
        BigDecimal totalCurrentValue
    ) {}

    public record RecordReceiptRequest(
        String skuId,
        int quantity,
        BigDecimal unitCost
    ) {}

    public record RecordReceiptResponse(
        long transactionId,
        OffsetDateTime transactionTimestamp,
        String skuId,
        String transactionType,
        int quantityChange,
        BigDecimal unitCost,
        BigDecimal totalAmountImpact
    ) {}
}
