ALTER TABLE users
    ADD COLUMN notification_email VARCHAR(100) NULL,
    ADD COLUMN notification_email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN notification_email_code_hash VARCHAR(64) NULL,
    ADD COLUMN notification_email_code_expires_at DATETIME NULL,
    ADD COLUMN notification_email_code_sent_at DATETIME NULL,
    ADD COLUMN notification_email_code_attempts INT NOT NULL DEFAULT 0;
