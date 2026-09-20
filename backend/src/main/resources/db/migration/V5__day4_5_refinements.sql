-- 1. Users table updates
ALTER TABLE users
ADD COLUMN gmail_connection_status VARCHAR(20) DEFAULT 'DISCONNECTED'
CHECK (gmail_connection_status IN ('CONNECTED', 'DISCONNECTED', 'NEEDS_RECONNECT'));

ALTER TABLE users
ADD COLUMN last_synced_at TIMESTAMP;

-- Migration data update for Users
UPDATE users
SET gmail_connection_status = 'NEEDS_RECONNECT'
WHERE encrypted_refresh_token IS NOT NULL;

-- 2. Suggested Applications UNIQUE constraint
ALTER TABLE suggested_applications
ADD CONSTRAINT uq_suggested_apps_raw_email UNIQUE (raw_email_id);

-- 3. Applications table updates
ALTER TABLE applications
ADD COLUMN source_raw_email_id BIGINT;

ALTER TABLE applications
ADD CONSTRAINT fk_applications_raw_email
FOREIGN KEY (source_raw_email_id)
REFERENCES raw_emails(id)
ON DELETE SET NULL;
