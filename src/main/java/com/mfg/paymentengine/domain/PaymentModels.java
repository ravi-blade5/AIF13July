package com.mfg.paymentengine.domain;

import com.mfg.paymentengine.error.ApiExceptionHandler.DomainValidationException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Traceability: MFG-ARC-PAY-CORE-001 v1.0 / PE-001..PE-012 / F-001,F-002,F-003. */
public final class PaymentModels {
    private PaymentModels() {}

    public enum PaymentRail {
        SCT, SCT_INST, SDD_CORE, SDD_B2B;
        public static PaymentRail fromExternal(String value) {
            try { return PaymentRail.valueOf(value); }
            catch (RuntimeException ex) { throw new DomainValidationException("INVALID_PAYMENT_RAIL", "Unsupported payment rail: " + value); }
        }
    }

    public enum PaymentStatus { RECEIVED, VALIDATED, VALIDATION_REJECTED, SCREENING_ALLOWED, SCREENING_FLAGGED, SCREENING_BLOCKED, FRAUD_ALLOWED, FRAUD_FLAGGED, FRAUD_BLOCKED, LEDGER_RESERVED, MESSAGE_READY, SUBMITTED_TO_SETTLEMENT, SETTLEMENT_ACCEPTED, SETTLEMENT_REJECTED, FINALISED, REVERSED, RETURNED, RECONCILED, REPORTABLE }
    public enum DecisionOutcome { ALLOW, FLAG, ESCALATE, BLOCK }
    public enum IdempotencyStatus { CREATED, REPLAYED }

    public static final class PaymentInstruction {
        private String paymentId;
        private String correlationId;
        private PaymentRail rail;
        private PaymentStatus status;
        private DecisionOutcome decisionOutcome;
        private String debtorIban;
        private String creditorIban;
        private String creditorName;
        private BigDecimal amount;
        private String currency;
        private String remittanceInformation;
        private String isoMessageType;
        private String isoMessagePayload;
        private String settlementRoute;
        private String settlementAcknowledgement;
        private Instant createdAt = Instant.now();
        private Instant updatedAt = createdAt;
        private final Map<String, Object> traceability = new HashMap<>();
        public void markUpdated() { this.updatedAt = Instant.now(); }
        public String getPaymentId() { return paymentId; }
        public void setPaymentId(String paymentId) { this.paymentId = paymentId; }
        public String getCorrelationId() { return correlationId; }
        public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
        public PaymentRail getRail() { return rail; }
        public void setRail(PaymentRail rail) { this.rail = rail; }
        public PaymentStatus getStatus() { return status; }
        public void setStatus(PaymentStatus status) { this.status = status; markUpdated(); }
        public DecisionOutcome getDecisionOutcome() { return decisionOutcome; }
        public void setDecisionOutcome(DecisionOutcome decisionOutcome) { this.decisionOutcome = decisionOutcome; }
        public String getDebtorIban() { return debtorIban; }
        public void setDebtorIban(String debtorIban) { this.debtorIban = debtorIban; }
        public String getCreditorIban() { return creditorIban; }
        public void setCreditorIban(String creditorIban) { this.creditorIban = creditorIban; }
        public String getCreditorName() { return creditorName; }
        public void setCreditorName(String creditorName) { this.creditorName = creditorName; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
        public String getRemittanceInformation() { return remittanceInformation; }
        public void setRemittanceInformation(String remittanceInformation) { this.remittanceInformation = remittanceInformation; }
        public String getIsoMessageType() { return isoMessageType; }
        public void setIsoMessageType(String isoMessageType) { this.isoMessageType = isoMessageType; }
        public String getIsoMessagePayload() { return isoMessagePayload; }
        public void setIsoMessagePayload(String isoMessagePayload) { this.isoMessagePayload = isoMessagePayload; }
        public String getSettlementRoute() { return settlementRoute; }
        public void setSettlementRoute(String settlementRoute) { this.settlementRoute = settlementRoute; }
        public String getSettlementAcknowledgement() { return settlementAcknowledgement; }
        public void setSettlementAcknowledgement(String settlementAcknowledgement) { this.settlementAcknowledgement = settlementAcknowledgement; }
        public Instant getCreatedAt() { return createdAt; }
        public Instant getUpdatedAt() { return updatedAt; }
        public Map<String, Object> getTraceability() { return traceability; }
        public void requireSettlementReady() {
            if (status != PaymentStatus.MESSAGE_READY && status != PaymentStatus.LEDGER_RESERVED) {
                throw new DomainValidationException("PAYMENT_NOT_SETTLEMENT_READY", "Payment is not ready for settlement: " + status);
            }
        }
        @Override public boolean equals(Object o) { return o instanceof PaymentInstruction that && Objects.equals(paymentId, that.paymentId); }
        @Override public int hashCode() { return Objects.hash(paymentId); }
    }

    public static final class BatchRecord {
        private String batchId;
        private String correlationId;
        private String status;
        private int acceptedCount;
        private int rejectedCount;
        private final List<BatchInstructionResult> instructionResults = new ArrayList<>();
        private final Map<String, Object> traceability = new HashMap<>();
        public String getBatchId() { return batchId; }
        public void setBatchId(String batchId) { this.batchId = batchId; }
        public String getCorrelationId() { return correlationId; }
        public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public int getAcceptedCount() { return acceptedCount; }
        public void setAcceptedCount(int acceptedCount) { this.acceptedCount = acceptedCount; }
        public int getRejectedCount() { return rejectedCount; }
        public void setRejectedCount(int rejectedCount) { this.rejectedCount = rejectedCount; }
        public List<BatchInstructionResult> getInstructionResults() { return instructionResults; }
        public Map<String, Object> getTraceability() { return traceability; }
    }

    public record BatchInstructionResult(String instructionReference, String status, String reason) {}
    public record ValidationResult(boolean valid, String reason) { public static ValidationResult ok() { return new ValidationResult(true, "OK"); } public static ValidationResult rejected(String reason) { return new ValidationResult(false, reason); } }
    public record DecisionResult(DecisionOutcome outcome, String reason, int score) {}
    public record IsoMessage(String messageType, String payload) {}
    public record LiquidityDecision(boolean approved, String reason) {}
    public record SettlementResult(String route, String status, String acknowledgementReference) {}
}
