CREATE TABLE deposits(
    id BIGSERIAL PRIMARY KEY,
    wallet_id BIGINT NOT NULL ,
    amount NUMERIC(19,2) NOT NULL,
    interest_rate NUMERIC(5,2) NOT NULL,
    term_months INT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    deposit_status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

        CONSTRAINT fk_deposit_wallet
                    FOREIGN KEY (wallet_id)
                    REFERENCES wallets(id)

)
