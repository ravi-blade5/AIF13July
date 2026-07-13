package com.aif.mfg.paymentengine.idempotency;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "idempotency_records")
public class IdempotencyRecord {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "idempotency_key", nullable = false, unique = true, length = 128)
    private String idempotencyKey;

    @Column(name = "request_hash", nullable = false, length = 64)
    private String requestHash;

    @Column(name = "payment_instruction_id", nullable = false)
    private UUID paymentInstructionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private IdempotencyStatus status;

    @Column(name = "response_body", nullable = false, columnDefinition = "text")
    private String responseBody;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    protected IdempotencyRecord() {
    }

    public static IdempotencyRecord completed(String idempotencyKey, String requestHash, UUID paymentInstructionId, String responseBody, Instant createdAt, Instant expiresAt) {
        IdempotencyRecord record = new IdempotencyRecord();
        record.id = UUID.randomUUID();
        record.idempotencyKey = idempotencyKey;
        record.requestHash = requestHash;
        record.paymentInstructionId = paymentInstructionId;
        record.status = IdempotencyStatus.COMPLETED;
        record.responseBody = responseBody;
        record.createdAt = createdAt;
        record.expiresAt = expiresAt;
        return record;
    }

    public String getIdempotencyKey() { return idempotencyKey; }
    public String getRequestHash() { return requestHash; }
    public UUID getPaymentInstructionId() { return paymentInstructionId; }
    public String getResponseBody() { return responseBody; }
    public Instant getExpiresAt() { return expiresAt; }
}
