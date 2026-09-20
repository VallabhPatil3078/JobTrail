CREATE TABLE suggested_applications (
    id BIGSERIAL PRIMARY KEY,
    raw_email_id BIGINT NOT NULL REFERENCES raw_emails(id) ON DELETE CASCADE,
    extracted_company VARCHAR(255),
    extracted_role VARCHAR(255),
    extracted_date DATE,
    confidence_score NUMERIC(5,2),
    status suggestion_status NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
