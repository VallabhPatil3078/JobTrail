CREATE TABLE raw_emails (
    id BIGSERIAL PRIMARY KEY,
    message_id VARCHAR(255) UNIQUE NOT NULL,
    subject TEXT,
    sender VARCHAR(255),
    snippet TEXT,
    received_at TIMESTAMP,
    processed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
