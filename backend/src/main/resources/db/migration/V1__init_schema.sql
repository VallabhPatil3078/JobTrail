CREATE TYPE application_status AS ENUM ('APPLIED', 'OA', 'INTERVIEW', 'OFFER', 'REJECTED', 'WITHDRAWN');
CREATE TYPE data_source AS ENUM ('MANUAL', 'EMAIL_DETECTED');
CREATE TYPE suggestion_status AS ENUM ('PENDING', 'CONFIRMED', 'REJECTED');

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    encrypted_refresh_token VARCHAR(512),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE applications (
    id BIGSERIAL PRIMARY KEY,
    company VARCHAR(255) NOT NULL,
    role VARCHAR(255) NOT NULL,
    job_description TEXT,
    job_url VARCHAR(512),
    status application_status DEFAULT 'APPLIED',
    source data_source NOT NULL,
    confidence NUMERIC(5,2),
    date_applied DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);