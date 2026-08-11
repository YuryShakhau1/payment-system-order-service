CREATE TABLE IF NOT EXISTS order_items (
    id            UUID PRIMARY KEY,
    order_id      UUID NOT NULL,
    item_id       UUID NOT NULL,
    quantity      INTEGER NOT NULL,
    item_price    NUMERIC(19, 2) NOT NULL,
    created_at    TIMESTAMP    NOT NULL,
    updated_at    TIMESTAMP    NOT NULL,

    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(id),
    CONSTRAINT fk_order_items_item FOREIGN KEY (item_id) REFERENCES items(id)
);

CREATE INDEX IF NOT EXISTS idx_order_items_item ON order_items(item_id);
CREATE INDEX IF NOT EXISTS idx_order_items_order_item ON order_items(order_id, item_id);
