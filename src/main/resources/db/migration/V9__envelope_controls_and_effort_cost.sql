ALTER TABLE envelopes
    ADD COLUMN fixed_envelope BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE user_finance_settings
    ADD COLUMN hourly_wage DECIMAL(19, 0) NOT NULL DEFAULT 0;

ALTER TABLE transactions
    ADD COLUMN non_essential BOOLEAN NOT NULL DEFAULT FALSE;
