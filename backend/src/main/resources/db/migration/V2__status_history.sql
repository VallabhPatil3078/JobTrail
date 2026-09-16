CREATE TABLE status_history (
    id BIGSERIAL PRIMARY KEY,
    application_id BIGINT NOT NULL,
    from_status application_status NOT NULL,
    to_status application_status NOT NULL,
    changed_at TIMESTAMP NOT NULL,
    note TEXT,
    source data_source NOT NULL,
    CONSTRAINT fk_application
        FOREIGN KEY(application_id)
        REFERENCES applications(id)
        ON DELETE CASCADE
);
