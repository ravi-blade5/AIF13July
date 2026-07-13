package com.mfg.paymentengine.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/** Traceability: BR-PE-001..BR-PE-012 / AC-001..AC-025. */
public final class PaymentDtos {
    private PaymentDtos() {}

    public record PaymentInstructionRequest(
            @NotBlank @Size(max = 34) String debtorIban,
            @NotBlank @Size(max = 34) String creditorIban,
            @NotBlank @Size(max = 140) String creditorName,
            @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
            @NotBlank @Pattern(regexp = "EUR") String currency,
            @Size(max = 140) String remittanceInformation) {}

    public record DirectDebitBatchRequest(
            @NotBlank @Size(max = 64) String corporateId,
            @NotBlank @Size(max = 128) String batchReference,
            @NotNull LocalDate requestedCollectionDate,
            @NotEmpty @Valid List<DirectDebitInstruction> instructions) {}

    public record DirectDebitInstruction(
            @NotBlank @Size(max = 64) String instructionReference,
            @NotBlank @Size(max = 64) String mandateReference,
            @NotBlank @Size(max = 34) String debtorIban,
            @NotBlank @Size(max = 34) String creditorIban,
            @NotBlank @Size(max = 140) String debtorName,
            @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
            @NotBlank @Pattern(regexp = "EUR") String currency) {}

    public record SettlementRequest(@NotBlank String rail) {}
    public record ExceptionEventRequest(@NotBlank String paymentId, @NotBlank String eventType, @NotBlank String reasonCode, @NotBlank String externalEventId) {}
    public record ReconciliationRequest(@NotBlank String settlementReportId, @NotNull LocalDate businessDate) {}

    public record PaymentResponse(String paymentId, String correlationId, String rail, String status, String decisionOutcome, String isoMessageType, String idempotencyStatus, Instant createdAt, Map<String, Object> traceability) {}
    public record BatchResponse(String batchId, String correlationId, String status, String idempotencyStatus, int acceptedCount, int rejectedCount, List<InstructionResult> instructionResults, Map<String, Object> traceability) {}
    public record InstructionResult(String instructionReference, String status, String reason) {}
    public record SettlementResponse(String paymentId, String settlementRoute, String settlementStatus, String acknowledgementReference) {}
    public record ExceptionEventResponse(String paymentId, String eventType, String status, String reason) {}
    public record ReconciliationResponse(String reconciliationId, String settlementReportId, String status, int matchedCount, int breakCount) {}
    public record ReportingSummaryResponse(long totalPayments, BigDecimal totalAmount, Map<String, Long> countByStatus, Instant generatedAt, Map<String, Object> traceability) {}
}
