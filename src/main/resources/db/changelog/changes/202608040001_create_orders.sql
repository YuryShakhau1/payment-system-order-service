CREATE TABLE IF NOT EXISTS orders (
    id           UUID PRIMARY KEY,
    user_id      UUID  NOT NULL,
    status       INTEGER NOT NULL,
    total_price  NUMERIC(19, 2) NOT NULL,
    deleted      BOOLEAN      NOT NULL,
    created_at   TIMESTAMP    NOT NULL,
    updated_at   TIMESTAMP    NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_orders_user ON orders(user_id);
CREATE INDEX IF NOT EXISTS idx_orders_created_at_status ON orders(created_at, status);
CREATE INDEX IF NOT EXISTS idx_orders_user_created_at_status ON orders(user_id, created_at, status);
