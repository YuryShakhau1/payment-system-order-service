CREATE TABLE IF NOT EXISTS product_snapshots (
    id            UUID PRIMARY KEY,
    product_id    UUID NOT NULL,
    name          VARCHAR(50) NOT NULL,
    price         NUMERIC(19, 2) NOT NULL,
    created_at   TIMESTAMP    NOT NULL,
    updated_at   TIMESTAMP    NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_product_snapshots_product_price ON product_snapshots(product_id, price);
