-- last line of defence: the database itself refuses negative balances and non-positive amounts
ALTER TABLE wallets
    ADD CONSTRAINT chk_wallet_balance_non_negative CHECK (balance >= 0);

ALTER TABLE deposits
    ADD CONSTRAINT chk_deposit_amount_positive CHECK (amount > 0),
    ADD CONSTRAINT chk_deposit_term CHECK (term_months BETWEEN 1 AND 24);

ALTER TABLE transaction
    ADD CONSTRAINT chk_transaction_amount_positive CHECK (amount > 0);

-- foreign keys are not indexed automatically in PostgreSQL
-- (wallets.user_id is already covered by uk_wallet_user_currency)
CREATE INDEX idx_deposits_wallet_id ON deposits (wallet_id);
CREATE INDEX idx_transaction_wallet_id ON transaction (wallet_id, created_at);
