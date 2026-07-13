CREATE TABLE payment_instructions (
    id UUID PRIMARY KEY,
    correlation_id VARCHAR(128) NOT NULL,
    source_channel VARCHAR(40) NOT NULL,
    rail VARCHAR(40) NOT NULL,
    status VARCHAR(40) NOT NULL,
    debtor_account_ref VARCHAR(64) NOT NULL,
    creditor_account_ref VARCHAR(64) NOT NULL,
    creditor_iban VARCHAR(34) NOT NULL,
    creditor_bic VARCHAR(11),
    amount NUMERIC(19, 4) NOT NULL,
    currency CHAR(3) NOT NULL,
    requested_execution_date DATE,
    remittance_information VARCHAR(140),
    psd2_consent_reference VARCHAR(128),
    sca_authentication_context VARCHAR(128),
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT chk_payment_instructions_amount_positive CHECK (amount > 0),
    CONSTRAINT chk_payment_instructions_status CHECK (status IN ('ACCEPTED', 'VALIDATION_PENDING', 'ORCHESTRATION_PENDING', 'REJECTED')),
    CONSTRAINT chk_payment_instructions_rail CHECK (rail IN ('SCT', 'SCT_INSTANT', 'SDD_CORE', 'SDD_B2B')),
    CONSTRAINT chk_payment_instructions_source_channel CHECK (source_channel IN ('MOBILE', 'WEB', 'CORPORATE_PORTAL', 'CONTACT_CENTRE', 'IVR', 'TPP', 'CORPORATE_H2H', 'BATCH'))
);

CREATE INDEX idx_payment_instructions_correlation_id ON payment_instructions (correlation_id);
CREATE INDEX idx_payment_instructions_created_at ON payment_instructions (created_at);
CREATE INDEX idx_payment_instructions_rail_status ON payment_instructions (rail, status);

CREATE TABLE idempotency_records (
    id UUID PRIMARY KEY,
    idempotency_key VARCHAR(128) NOT NULL UNIQUE,
    request_hash CHAR(64) NOT NULL,
    payment_instruction_id UUID NOT NULL REFERENCES payment_instructions(id),
    status VARCHAR(30) NOT NULL,
    response_body TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT chk_idempotency_records_status CHECK (status IN ('COMPLETED')),
    CONSTRAINT chk_idempotency_records_hash_length CHECK (char_length(request_hash) = 64)
);

CREATE INDEX idx_idempotency_records_expires_at ON idempotency_records (expires_at);
CREATE INDEX idx_idempotency_records_payment_instruction_id ON idempotency_records (payment_instruction_id);
