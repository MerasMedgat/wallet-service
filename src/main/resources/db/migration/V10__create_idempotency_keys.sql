CREATE TABLE idempotency_keys
(
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT       NOT NULL REFERENCES users (id),
    idempotency_key VARCHAR(100) NOT NULL,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_idempotency_user_key UNIQUE (user_id, idempotency_key)
);
