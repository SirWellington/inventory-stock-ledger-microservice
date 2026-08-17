package com.sirwellington.target.db;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import com.sirwellington.target.model.TransactionType;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.SQLDialect;
import org.jooq.Table;
import org.jooq.impl.DSL;

public class InventoryRepository {

    public record InsertTransactionRequest(
        TransactionType type,
        String skuId,
        int quantityChange,
        BigDecimal unitCost
    ) {}

    public record InsertTransactionResponse(
        long transactionId,
        OffsetDateTime transactionTimestamp
    ) {}

    public record GetInventoryValueRequest(
        String skuId
    ) {}

    public record GetInventoryValueResponse(
        int currentQuantity,
        BigDecimal totalCurrentValue
    ) {}

    public record GetLedgerHistoryQuery(
        OffsetDateTime startDate,
        OffsetDateTime endDate
    ) {}

    public record TransactionRecord(
        long transactionId,
        OffsetDateTime transactionTimestamp,
        String skuId,
        String transactionType,
        int quantityChange,
        BigDecimal unitCost,
        BigDecimal totalAmountImpact
    ) {}

    public record GetLedgerHistoryResponse(
        List<TransactionRecord> transactions
    ) {}

    private static final Table<?> INVENTORY_TRANSACTIONS = DSL.table(
        DSL.name("inventory_transactions")
    );

    private static final Field<Long> TRANSACTION_ID = DSL.field(
        "transaction_id",
        Long.class
    );
    private static final Field<OffsetDateTime> TRANSACTION_TIMESTAMP = DSL.field(
        "transaction_timestamp",
        OffsetDateTime.class
    );
    private static final Field<String> SKU_ID = DSL.field(
        "sku_id",
        String.class
    );
    private static final Field<String> TRANSACTION_TYPE = DSL.field(
        "transaction_type",
        String.class
    );
    private static final Field<Integer> QUANTITY_CHANGE = DSL.field(
        "quantity_change",
        Integer.class
    );
    private static final Field<BigDecimal> UNIT_COST = DSL.field(
        "unit_cost",
        BigDecimal.class
    );
    private static final Field<BigDecimal> TOTAL_AMOUNT_IMPACT = DSL.field(
        "total_amount_impact",
        BigDecimal.class
    );

    private static final Table<?> SKU_INVENTORY_SNAPSHOTS = DSL.table(
        DSL.name("sku_inventory_snapshots")
    );

    private static final Field<Integer> CURRENT_QUANTITY = DSL.field(
        "current_quantity",
        Integer.class
    );
    private static final Field<BigDecimal> TOTAL_CURRENT_VALUE = DSL.field(
        "total_current_value",
        BigDecimal.class
    );

    private final DSLContext dsl;
    private final Connection conn;

    public InventoryRepository(Connection connection) {
        this.conn = connection;
        this.dsl = DSL.using(connection, SQLDialect.POSTGRES);
    }

    public InsertTransactionResponse insertTransaction(InsertTransactionRequest request) {
        OffsetDateTime now = OffsetDateTime.now();

        try (var ps = conn.prepareStatement(
                "INSERT INTO inventory_transactions (sku_id, transaction_type, quantity_change, unit_cost, transaction_timestamp) VALUES (?, ?, ?, ?, ?)",
                PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, request.skuId());
            ps.setString(2, request.type().name());
            ps.setInt(3, request.quantityChange());
            ps.setBigDecimal(4, request.unitCost());
            ps.setObject(5, now);
            ps.executeUpdate();

            try (var rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return new InsertTransactionResponse(rs.getLong(1), now);
                } else {
                    throw new RuntimeException("No generated keys returned");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<GetInventoryValueResponse> getInventoryValue(GetInventoryValueRequest request) {
        var result = dsl.select(CURRENT_QUANTITY, TOTAL_CURRENT_VALUE)
                        .from(SKU_INVENTORY_SNAPSHOTS)
                        .where(SKU_ID.eq(request.skuId()))
                        .fetchOptional();

        return result.map(record -> new GetInventoryValueResponse(
            record.get(CURRENT_QUANTITY),
            record.get(TOTAL_CURRENT_VALUE)
        ));
    }

    public GetLedgerHistoryResponse getLedgerHistory(GetLedgerHistoryQuery query) {
        var records = dsl.select(
                            TRANSACTION_ID,
                            TRANSACTION_TIMESTAMP,
                            SKU_ID,
                            TRANSACTION_TYPE,
                            QUANTITY_CHANGE,
                            UNIT_COST,
                            TOTAL_AMOUNT_IMPACT
                        )
                         .from(INVENTORY_TRANSACTIONS)
                         .where(TRANSACTION_TIMESTAMP.ge(query.startDate()))
                         .and(TRANSACTION_TIMESTAMP.le(query.endDate()))
                         .orderBy(TRANSACTION_TIMESTAMP.asc())
                         .fetch()
                         .map(record -> new TransactionRecord(
                             record.get(TRANSACTION_ID),
                             record.get(TRANSACTION_TIMESTAMP),
                             record.get(SKU_ID),
                             record.get(TRANSACTION_TYPE),
                             record.get(QUANTITY_CHANGE),
                             record.get(UNIT_COST),
                             record.get(TOTAL_AMOUNT_IMPACT)
                         ));

        return new GetLedgerHistoryResponse(records);
    }
}
