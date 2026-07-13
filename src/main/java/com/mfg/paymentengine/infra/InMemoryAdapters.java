package com.mfg.paymentengine.infra;

import com.mfg.paymentengine.config.PaymentEngineProperties;
import com.mfg.paymentengine.domain.PaymentModels.BatchRecord;
import com.mfg.paymentengine.domain.PaymentModels.PaymentInstruction;
import com.mfg.paymentengine.service.Ports.BatchRepository;
import com.mfg.paymentengine.service.Ports.IdempotencyRecord;
import com.mfg.paymentengine.service.Ports.IdempotencyStore;
import com.mfg.paymentengine.service.Ports.PaymentRepository;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** Local in-memory adapters. Production replacements: DB-005 Redis, OLTP store, event store. */
public final class InMemoryAdapters {
    private InMemoryAdapters() {}

    @Repository
    public static class InMemoryPaymentRepository implements PaymentRepository {
        private final ConcurrentHashMap<String, PaymentInstruction> payments = new ConcurrentHashMap<>();
        @Override public PaymentInstruction save(PaymentInstruction instruction) { payments.put(instruction.getPaymentId(), instruction); return instruction; }
        @Override public Optional<PaymentInstruction> findById(String paymentId) { return Optional.ofNullable(payments.get(paymentId)); }
        @Override public List<PaymentInstruction> findAll() { return payments.values().stream().sorted(Comparator.comparing(PaymentInstruction::getCreatedAt)).toList(); }
    }

    @Repository
    public static class InMemoryBatchRepository implements BatchRepository {
        private final ConcurrentHashMap<String, BatchRecord> batches = new ConcurrentHashMap<>();
        @Override public BatchRecord save(BatchRecord record) { batches.put(record.getBatchId(), record); return record; }
        @Override public Optional<BatchRecord> findById(String batchId) { return Optional.ofNullable(batches.get(batchId)); }
    }

    @Repository
    public static class InMemoryIdempotencyStore implements IdempotencyStore {
        private final ConcurrentHashMap<String, IdempotencyRecord> records = new ConcurrentHashMap<>();
        private final Duration ttl;
        public InMemoryIdempotencyStore(PaymentEngineProperties properties) { this.ttl = Duration.ofHours(properties.idempotency().ttlHours()); }
        @Override public Optional<IdempotencyRecord> find(String key) { purgeExpired(); return Optional.ofNullable(records.get(key)); }
        @Override public IdempotencyRecord store(String key, String requestFingerprint, String responseType, String responseReference) { purgeExpired(); IdempotencyRecord record = new IdempotencyRecord(key, requestFingerprint, responseType, responseReference, Instant.now().plus(ttl)); records.put(key, record); return record; }
        private void purgeExpired() { Instant now = Instant.now(); records.entrySet().removeIf(entry -> entry.getValue().expiresAt().isBefore(now)); }
    }
}
