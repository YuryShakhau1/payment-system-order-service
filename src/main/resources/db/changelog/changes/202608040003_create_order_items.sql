CREATE TABLE IF NOT EXISTS order_items (
    id                     UUID PRIMARY KEY,
    order_id               UUID NOT NULL,
    product_id             UUID NOT NULL,
    product_snapshot_id    UUID NOT NULL,
    quantity               BIGINT NOT NULL,
    item_price             NUMERIC(19, 2) NOT NULL,
    created_at             TIMESTAMP    NOT NULL,
    updated_at             TIMESTAMP    NOT NULL,

    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(id),
    CONSTRAINT fk_order_items_product_snapshot FOREIGN KEY (product_snapshot_id) REFERENCES product_snapshots(id)
);

CREATE INDEX IF NOT EXISTS idx_order_items_order ON order_items(order_id);
CREATE INDEX IF NOT EXISTS idx_order_items_product_snapshot ON order_items(product_snapshot_id);
