package com.mfg.paymentengine.service;

import com.mfg.paymentengine.domain.PaymentModels.BatchRecord;
import com.mfg.paymentengine.domain.PaymentModels.DecisionResult;
import com.mfg.paymentengine.domain.PaymentModels.IsoMessage;
import com.mfg.paymentengine.domain.PaymentModels.LiquidityDecision;
import com.mfg.paymentengine.domain.PaymentModels.PaymentInstruction;
import com.mfg.paymentengine.domain.PaymentModels.PaymentRail;
import com.mfg.paymentengine.domain.PaymentModels.SettlementResult;
import com.mfg.paymentengine.domain.PaymentModels.ValidationResult;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/** Ports for Payment Engine clean architecture boundaries. */
public interface Ports {
    interface PaymentRepository { PaymentInstruction save(PaymentInstruction instruction); Optional<PaymentInstruction> findById(String paymentId); List<PaymentInstruction> findAll(); }
    interface BatchRepository { BatchRecord save(BatchRecord record); Optional<BatchRecord> findById(String batchId); }
    interface IdempotencyStore { Optional<IdempotencyRecord> find(String key); IdempotencyRecord store(String key, String requestFingerprint, String responseType, String responseReference); }
    record IdempotencyRecord(String key, String requestFingerprint, String responseType, String responseReference, Instant expiresAt) {}
    interface DomainEventPublisher { void publish(String topic, String correlationId, Object payload); }
    interface ValidationEngine { ValidationResult validate(PaymentInstruction instruction); }
    interface SanctionsScreening { DecisionResult screen(PaymentInstruction instruction); }
    interface FraudScoring { DecisionResult score(PaymentInstruction instruction); }
    interface Iso20022Broker { IsoMessage prepare(PaymentInstruction instruction); }
    interface MandateManagement { boolean isMandateValid(String mandateReference, String debtorIban, String creditorIban); }
    interface LiquidityManager { LiquidityDecision check(PaymentInstruction instruction); }
    interface SettlementAdapter { SettlementResult submit(PaymentInstruction instruction, PaymentRail rail); }
}
