CREATE TABLE IF NOT EXISTS cached_users (
    id         UUID PRIMARY KEY,
    email      VARCHAR(100) NOT NULL UNIQUE,
    first_name VARCHAR(50),
    last_name  VARCHAR(50)
);
