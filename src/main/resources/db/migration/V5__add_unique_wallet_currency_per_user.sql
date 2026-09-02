ALTER TABLE wallets
    ADD CONSTRAINT uk_wallet_user_currency
        UNIQUE (user_id, currency);