package com.mfg.paymentengine.service;

import com.mfg.paymentengine.api.dto.PaymentDtos.BatchResponse;
import com.mfg.paymentengine.api.dto.PaymentDtos.DirectDebitBatchRequest;
import com.mfg.paymentengine.api.dto.PaymentDtos.ExceptionEventResponse;
import com.mfg.paymentengine.api.dto.PaymentDtos.InstructionResult;
import com.mfg.paymentengine.api.dto.PaymentDtos.PaymentInstructionRequest;
import com.mfg.paymentengine.api.dto.PaymentDtos.PaymentResponse;
import com.mfg.paymentengine.api.dto.PaymentDtos.ReconciliationResponse;
import com.mfg.paymentengine.api.dto.PaymentDtos.ReportingSummaryResponse;
import com.mfg.paymentengine.api.dto.PaymentDtos.SettlementResponse;
import com.mfg.paymentengine.config.PaymentEngineProperties;
import com.mfg.paymentengine.domain.PaymentModels.*;
import com.mfg.paymentengine.error.ApiExceptionHandler.CodedDomainException;
import com.mfg.paymentengine.error.ApiExceptionHandler.DomainConflictException;
import com.mfg.paymentengine.error.ApiExceptionHandler.DomainNotFoundException;
import com.mfg.paymentengine.error.ApiExceptionHandler.DomainValidationException;
import com.mfg.paymentengine.service.Ports.*;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Traceability: AIF-169..AIF-174 / PE-001..PE-012 / AC-001..AC-025. */
@Service
public class PaymentWorkflowService {
    private static final String PAYMENT_RESPONSE = "PAYMENT";
    private static final String BATCH_RESPONSE = "BATCH";
    private final PaymentRepository paymentRepository;
    private final BatchRepository batchRepository;
    private final IdempotencyStore idempotencyStore;
    private final DomainEventPublisher eventPublisher;
    private final ValidationEngine validationEngine;
    private final SanctionsScreening sanctionsScreening;
    private final FraudScoring fraudScoring;
    private final Iso20022Broker iso20022Broker;
    private final MandateManagement mandateManagement;
    private final LiquidityManager liquidityManager;
    private final SettlementAdapter settlementAdapter;
    private final PaymentEngineProperties properties;

    public PaymentWorkflowService(PaymentRepository paymentRepository, BatchRepository batchRepository, IdempotencyStore idempotencyStore, DomainEventPublisher eventPublisher, ValidationEngine validationEngine, SanctionsScreening sanctionsScreening, FraudScoring fraudScoring, Iso20022Broker iso20022Broker, MandateManagement mandateManagement, LiquidityManager liquidityManager, SettlementAdapter settlementAdapter, PaymentEngineProperties properties) {
        this.paymentRepository = paymentRepository;
        this.batchRepository = batchRepository;
        this.idempotencyStore = idempotencyStore;
        this.eventPublisher = eventPublisher;
        this.validationEngine = validationEngine;
        this.sanctionsScreening = sanctionsScreening;
        this.fraudScoring = fraudScoring;
        this.iso20022Broker = iso20022Broker;
        this.mandateManagement = mandateManagement;
        this.liquidityManager = liquidityManager;
        this.settlementAdapter = settlementAdapter;
        this.properties = properties;
    }

    public PaymentResponse createPayment(PaymentRail rail, PaymentInstructionRequest request, String idempotencyKey, String sourceChannel) {
        requireAllowedSource(sourceChannel);
        requireIdempotencyKey(idempotencyKey);
        String requestFingerprint = fingerprint(rail + ":" + request);
        IdempotencyRecord existing = idempotencyStore.find(idempotencyKey).orElse(null);
        if (existing != null) {
            requireSameFingerprint(existing, requestFingerprint);
            requireResponseType(existing, PAYMENT_RESPONSE);
            PaymentInstruction replay = paymentRepository.findById(existing.responseReference()).orElseThrow(() -> new DomainConflictException("IDEMPOTENCY_RECORD_ORPHANED", "Stored idempotency response no longer exists"));
            return toPaymentResponse(replay, IdempotencyStatus.REPLAYED);
        }
        PaymentInstruction instruction = newPayment(rail, request, sourceChannel);
        paymentRepository.save(instruction);
        eventPublisher.publish("payments.instructions.created", instruction.getCorrelationId(), safeEvent(instruction));
        orchestrateDecisioning(instruction);
        paymentRepository.save(instruction);
        idempotencyStore.store(idempotencyKey, requestFingerprint, PAYMENT_RESPONSE, instruction.getPaymentId());
        return toPaymentResponse(instruction, IdempotencyStatus.CREATED);
    }

    public BatchResponse createDirectDebitBatch(DirectDebitBatchRequest request, String idempotencyKey, String sourceChannel) {
        requireAllowedSource(sourceChannel);
        requireIdempotencyKey(idempotencyKey);
        String requestFingerprint = fingerprint("SDD_BATCH:" + request);
        IdempotencyRecord existing = idempotencyStore.find(idempotencyKey).orElse(null);
        if (existing != null) {
            requireSameFingerprint(existing, requestFingerprint);
            requireResponseType(existing, BATCH_RESPONSE);
            BatchRecord replay = batchRepository.findById(existing.responseReference()).orElseThrow(() -> new DomainConflictException("IDEMPOTENCY_RECORD_ORPHANED", "Stored batch response no longer exists"));
            return toBatchResponse(replay, IdempotencyStatus.REPLAYED);
        }
        BatchRecord batch = new BatchRecord();
        batch.setBatchId("batch_" + UUID.randomUUID());
        batch.setCorrelationId("corr_" + UUID.randomUUID());
        attachBatchTraceability(batch);
        int accepted = 0;
        int rejected = 0;
        for (var item : request.instructions()) {
            if (!mandateManagement.isMandateValid(item.mandateReference(), item.debtorIban(), item.creditorIban())) {
                rejected++;
                batch.getInstructionResults().add(new BatchInstructionResult(item.instructionReference(), "REJECTED", "MANDATE_INVALID"));
                continue;
            }
            PaymentInstruction instruction = new PaymentInstruction();
            instruction.setPaymentId("pay_" + UUID.randomUUID());
            instruction.setCorrelationId(batch.getCorrelationId());
            instruction.setRail(PaymentRail.SDD_CORE);
            instruction.setStatus(PaymentStatus.RECEIVED);
            instruction.setDebtorIban(item.debtorIban());
            instruction.setCreditorIban(item.creditorIban());
            instruction.setCreditorName(item.debtorName());
            instruction.setAmount(item.amount());
            instruction.setCurrency(item.currency());
            instruction.setRemittanceInformation("SDD batch " + request.batchReference());
            attachPaymentTraceability(instruction, sourceChannel, List.of("PE-001", "PE-002", "PE-003", "PE-005", "PE-006", "PE-009"), List.of("F-002"));
            instruction.getTraceability().put("batch_id", batch.getBatchId());
            instruction.getTraceability().put("mandate_reference", item.mandateReference());
            try {
                orchestrateSddInstruction(instruction, request.requestedCollectionDate());
                paymentRepository.save(instruction);
                accepted++;
                batch.getInstructionResults().add(new BatchInstructionResult(item.instructionReference(), instruction.getStatus().name(), "OK"));
            } catch (CodedDomainException ex) {
                paymentRepository.save(instruction);
                rejected++;
                batch.getInstructionResults().add(new BatchInstructionResult(item.instructionReference(), "REJECTED", ex.code()));
            }
        }
        batch.setAcceptedCount(accepted);
        batch.setRejectedCount(rejected);
        batch.setStatus(rejected == 0 ? "ACCEPTED" : accepted == 0 ? "REJECTED" : "PARTIALLY_ACCEPTED");
        batchRepository.save(batch);
        idempotencyStore.store(idempotencyKey, requestFingerprint, BATCH_RESPONSE, batch.getBatchId());
        eventPublisher.publish("payments.instructions.batch_processed", batch.getCorrelationId(), Map.of("batchId", batch.getBatchId(), "acceptedCount", accepted, "rejectedCount", rejected));
        return toBatchResponse(batch, IdempotencyStatus.CREATED);
    }

    public SettlementResponse submitSettlement(String paymentId, PaymentRail rail) {
        PaymentInstruction instruction = findPayment(paymentId);
        instruction.requireSettlementReady();
        LiquidityDecision decision = liquidityManager.check(instruction);
        if (!decision.approved()) {
            instruction.setStatus(PaymentStatus.SETTLEMENT_REJECTED);
            paymentRepository.save(instruction);
            eventPublisher.publish("payments.settlement.rejected", instruction.getCorrelationId(), Map.of("paymentId", paymentId, "reason", decision.reason()));
            throw new DomainConflictException("LIQUIDITY_BLOCKED", decision.reason());
        }
        instruction.setStatus(PaymentStatus.SUBMITTED_TO_SETTLEMENT);
        paymentRepository.save(instruction);
        SettlementResult result = settlementAdapter.submit(instruction, rail);
        instruction.setSettlementRoute(result.route());
        instruction.setSettlementAcknowledgement(result.acknowledgementReference());
        instruction.setStatus("ACSP".equalsIgnoreCase(result.status()) ? PaymentStatus.SETTLEMENT_ACCEPTED : PaymentStatus.SETTLEMENT_REJECTED);
        paymentRepository.save(instruction);
        eventPublisher.publish("payments.settlement.completed", instruction.getCorrelationId(), Map.of("paymentId", paymentId, "route", result.route(), "status", result.status()));
        return new SettlementResponse(paymentId, result.route(), result.status(), result.acknowledgementReference());
    }

    public ExceptionEventResponse handleExceptionEvent(String paymentId, String eventType, String reasonCode, String externalEventId) {
        PaymentInstruction instruction = findPayment(paymentId);
        if (reasonCode.length() < 2) throw new DomainValidationException("INVALID_REASON_CODE", "Reason code is too short");
        switch (eventType.toUpperCase()) {
            case "RETURN", "REJECT", "REFUND", "REVERSAL", "REVOCATION" -> instruction.setStatus(PaymentStatus.RETURNED);
            default -> throw new DomainValidationException("UNSUPPORTED_EXCEPTION_EVENT", "Unsupported R-transaction event type");
        }
        paymentRepository.save(instruction);
        eventPublisher.publish("payments.exceptions.received", instruction.getCorrelationId(), Map.of("paymentId", paymentId, "eventType", eventType, "reasonCode", reasonCode, "externalEventId", externalEventId));
        return new ExceptionEventResponse(paymentId, eventType, "PROCESSED", reasonCode);
    }

    public ReconciliationResponse reconcile(String settlementReportId, LocalDate businessDate) {
        List<PaymentInstruction> all = paymentRepository.findAll();
        long matched = all.stream().filter(p -> p.getStatus() == PaymentStatus.SETTLEMENT_ACCEPTED || p.getStatus() == PaymentStatus.FINALISED).count();
        long breaks = all.stream().filter(p -> p.getStatus() == PaymentStatus.SETTLEMENT_REJECTED || p.getStatus() == PaymentStatus.RETURNED).count();
        String reconciliationId = "recon_" + UUID.randomUUID();
        eventPublisher.publish("payments.reconciliation.break_detected", reconciliationId, Map.of("settlementReportId", settlementReportId, "businessDate", businessDate.toString(), "breakCount", breaks));
        return new ReconciliationResponse(reconciliationId, settlementReportId, "COMPLETED", Math.toIntExact(matched), Math.toIntExact(breaks));
    }

    public ReportingSummaryResponse reportSummary() {
        List<PaymentInstruction> all = paymentRepository.findAll();
        Map<PaymentStatus, Long> statusCounts = new EnumMap<>(PaymentStatus.class);
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (PaymentInstruction payment : all) {
            statusCounts.merge(payment.getStatus(), 1L, Long::sum);
            if (payment.getAmount() != null) totalAmount = totalAmount.add(payment.getAmount());
        }
        Map<String, Long> serialized = new LinkedHashMap<>();
        statusCounts.forEach((status, count) -> serialized.put(status.name(), count));
        eventPublisher.publish("payments.reporting.generated", "report_" + UUID.randomUUID(), Map.of("totalPayments", all.size(), "generatedAt", Instant.now().toString()));
        return new ReportingSummaryResponse(all.size(), totalAmount, serialized, Instant.now(), traceability(List.of("PE-010", "PE-011", "PE-012"), List.of("F-001", "F-002", "F-003")));
    }

    private PaymentInstruction newPayment(PaymentRail rail, PaymentInstructionRequest request, String sourceChannel) {
        PaymentInstruction instruction = new PaymentInstruction();
        instruction.setPaymentId("pay_" + UUID.randomUUID());
        instruction.setCorrelationId("corr_" + UUID.randomUUID());
        instruction.setRail(rail);
        instruction.setStatus(PaymentStatus.RECEIVED);
        instruction.setDebtorIban(request.debtorIban());
        instruction.setCreditorIban(request.creditorIban());
        instruction.setCreditorName(request.creditorName());
        instruction.setAmount(request.amount());
        instruction.setCurrency(request.currency());
        instruction.setRemittanceInformation(request.remittanceInformation());
        attachPaymentTraceability(instruction, sourceChannel, List.of("PE-001", "PE-002", "PE-003", "PE-004", "PE-005", "PE-006"), List.of("F-001", "F-003"));
        return instruction;
    }

    private void orchestrateDecisioning(PaymentInstruction instruction) {
        ValidationResult validation = validationEngine.validate(instruction);
        if (!validation.valid()) rejectAndThrow(instruction, PaymentStatus.VALIDATION_REJECTED, "VALIDATION_ERROR", validation.reason());
        instruction.setStatus(PaymentStatus.VALIDATED);
        paymentRepository.save(instruction);
        DecisionResult sanctions = sanctionsScreening.screen(instruction);
        if (sanctions.outcome() == DecisionOutcome.BLOCK) { instruction.setDecisionOutcome(DecisionOutcome.BLOCK); rejectAndThrow(instruction, PaymentStatus.SCREENING_BLOCKED, "SCREENING_BLOCKED", sanctions.reason()); }
        instruction.setStatus(sanctions.outcome() == DecisionOutcome.FLAG ? PaymentStatus.SCREENING_FLAGGED : PaymentStatus.SCREENING_ALLOWED);
        DecisionResult fraud = fraudScoring.score(instruction);
        if (fraud.outcome() == DecisionOutcome.BLOCK) { instruction.setDecisionOutcome(DecisionOutcome.BLOCK); rejectAndThrow(instruction, PaymentStatus.FRAUD_BLOCKED, "FRAUD_BLOCKED", fraud.reason()); }
        instruction.setDecisionOutcome(fraud.outcome());
        instruction.setStatus(fraud.outcome() == DecisionOutcome.FLAG ? PaymentStatus.FRAUD_FLAGGED : PaymentStatus.FRAUD_ALLOWED);
        instruction.setStatus(PaymentStatus.LEDGER_RESERVED);
        IsoMessage isoMessage = iso20022Broker.prepare(instruction);
        instruction.setIsoMessageType(isoMessage.messageType());
        instruction.setIsoMessagePayload(isoMessage.payload());
        instruction.setStatus(PaymentStatus.MESSAGE_READY);
        eventPublisher.publish("payments.orchestration.state_changed", instruction.getCorrelationId(), safeEvent(instruction));
    }

    private void orchestrateSddInstruction(PaymentInstruction instruction, LocalDate requestedCollectionDate) {
        if (requestedCollectionDate.isBefore(LocalDate.now())) rejectAndThrow(instruction, PaymentStatus.VALIDATION_REJECTED, "CUT_OFF_VIOLATION", "Requested collection date cannot be in the past");
        ValidationResult validation = validationEngine.validate(instruction);
        if (!validation.valid()) rejectAndThrow(instruction, PaymentStatus.VALIDATION_REJECTED, "VALIDATION_ERROR", validation.reason());
        DecisionResult sanctions = sanctionsScreening.screen(instruction);
        if (sanctions.outcome() == DecisionOutcome.BLOCK) rejectAndThrow(instruction, PaymentStatus.SCREENING_BLOCKED, "SCREENING_BLOCKED", sanctions.reason());
        instruction.setStatus(PaymentStatus.VALIDATED);
        IsoMessage isoMessage = iso20022Broker.prepare(instruction);
        instruction.setIsoMessageType(isoMessage.messageType());
        instruction.setIsoMessagePayload(isoMessage.payload());
        instruction.setStatus(PaymentStatus.MESSAGE_READY);
    }

    private void rejectAndThrow(PaymentInstruction instruction, PaymentStatus status, String code, String reason) {
        instruction.setStatus(status);
        paymentRepository.save(instruction);
        eventPublisher.publish("payments.instructions.rejected", instruction.getCorrelationId(), Map.of("paymentId", instruction.getPaymentId(), "status", status.name(), "reason", reason));
        if (status == PaymentStatus.VALIDATION_REJECTED) throw new DomainValidationException(code, reason);
        throw new DomainConflictException(code, reason);
    }

    private PaymentInstruction findPayment(String paymentId) { return paymentRepository.findById(paymentId).orElseThrow(() -> new DomainNotFoundException("PAYMENT_NOT_FOUND", "Payment not found: " + paymentId)); }
    private void requireAllowedSource(String sourceChannel) { if (sourceChannel == null || sourceChannel.isBlank()) throw new DomainValidationException("SOURCE_CHANNEL_REQUIRED", "X-Source-Channel header is required"); boolean allowed = properties.security().allowedSourceChannels().stream().anyMatch(value -> value.equalsIgnoreCase(sourceChannel)); if (!allowed) throw new DomainValidationException("UNAUTHORIZED_SOURCE", "Source channel is not approved: " + sourceChannel); }
    private void requireIdempotencyKey(String idempotencyKey) { if (idempotencyKey == null || idempotencyKey.isBlank()) throw new DomainValidationException("IDEMPOTENCY_KEY_REQUIRED", "Idempotency-Key header is required"); }
    private void requireSameFingerprint(IdempotencyRecord existing, String requestFingerprint) { if (!existing.requestFingerprint().equals(requestFingerprint)) throw new DomainConflictException("IDEMPOTENCY_CONFLICT", "Idempotency-Key was reused with a different payload"); }
    private void requireResponseType(IdempotencyRecord existing, String type) { if (!type.equals(existing.responseType())) throw new DomainConflictException("IDEMPOTENCY_TYPE_CONFLICT", "Idempotency-Key was reused for a different operation type"); }
    private String fingerprint(String input) { return DigestUtils.md5DigestAsHex(input.getBytes(StandardCharsets.UTF_8)); }
    private Map<String, Object> safeEvent(PaymentInstruction instruction) { return Map.of("paymentId", instruction.getPaymentId(), "correlationId", instruction.getCorrelationId(), "rail", instruction.getRail().name(), "status", instruction.getStatus().name()); }
    private void attachPaymentTraceability(PaymentInstruction instruction, String sourceChannel, List<String> components, List<String> flows) { instruction.getTraceability().putAll(traceability(components, flows)); instruction.getTraceability().put("source_channel", sourceChannel); }
    private void attachBatchTraceability(BatchRecord batch) { batch.getTraceability().putAll(traceability(List.of("PE-001", "PE-002", "PE-003", "PE-005", "PE-006", "PE-009"), List.of("F-002"))); }
    private Map<String, Object> traceability(List<String> componentIds, List<String> flowIds) { Map<String, Object> trace = new LinkedHashMap<>(); trace.put("source_doc_id", properties.traceability().sourceDocId()); trace.put("source_doc_version", properties.traceability().sourceDocVersion()); trace.put("source_file", properties.traceability().sourceFile()); trace.put("component_ids", componentIds); trace.put("flow_ids", flowIds); return trace; }
    private PaymentResponse toPaymentResponse(PaymentInstruction instruction, IdempotencyStatus idempotencyStatus) { return new PaymentResponse(instruction.getPaymentId(), instruction.getCorrelationId(), instruction.getRail().name(), instruction.getStatus().name(), instruction.getDecisionOutcome() == null ? null : instruction.getDecisionOutcome().name(), instruction.getIsoMessageType(), idempotencyStatus.name(), instruction.getCreatedAt(), instruction.getTraceability()); }
    private BatchResponse toBatchResponse(BatchRecord batch, IdempotencyStatus idempotencyStatus) { List<InstructionResult> results = batch.getInstructionResults().stream().map(item -> new InstructionResult(item.instructionReference(), item.status(), item.reason())).toList(); return new BatchResponse(batch.getBatchId(), batch.getCorrelationId(), batch.getStatus(), idempotencyStatus.name(), batch.getAcceptedCount(), batch.getRejectedCount(), results, batch.getTraceability()); }
}
