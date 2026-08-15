CREATE TABLE IF NOT EXISTS inventory_transactions (
    -- Primary Key for the transaction log
    transaction_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    -- Timestamp is crucial for financial reporting windows
    transaction_timestamp TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),

    -- The item identifier. This is an item that Target sells or holds inventory.
    sku_id VARCHAR(50) NOT NULL,

    -- What kind of event was this? (RECEIPT, SALE, ADJUSTMENT)
    transaction_type VARCHAR(100) NOT NULL,

    -- How many units moved? (Negative for sales/write-offs, Positive for receipts)
    quantity_change INTEGER NOT NULL,

    -- The financial value per unit at the time of this specific transaction
    unit_cost DECIMAL(10, 4) NOT NULL DEFAULT 0.00,

    -- A simple calculation showing the total dollar impact of this single event
    -- Total = Quantity x Unit-Cost
    total_amount_impact DECIMAL(15, 2) GENERATED ALWAYS AS (quantity_change * unit_cost) STORED
);

-- PERFORMANCE OPTIMIZATION:
-- Index for O(log N) lookups on historical data
-- This index solves the "slow query" problem by allowing us to instantly find all the history
-- for a specific item without scanning the whole table.
CREATE INDEX IF NOT EXISTS idx_transactions_sku ON inventory_transactions (sku_id);

CREATE TABLE IF NOT EXISTS sku_inventory_snapshots (
    -- Item identifier is the Primary Key for this table.
    sku_id VARCHAR(50) PRIMARY KEY,
    current_quantity INTEGER NOT NULL DEFAULT 0,
    total_current_value DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    last_updated TIMESTAMP WITH TIME ZONE DEFAULT now()
);
