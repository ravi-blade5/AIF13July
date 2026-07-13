package com.mfg.paymentengine.api;

import com.mfg.paymentengine.api.dto.PaymentDtos.*;
import com.mfg.paymentengine.domain.PaymentModels.PaymentRail;
import com.mfg.paymentengine.service.PaymentWorkflowService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/** Traceability: PE-001, PE-007, PE-008, PE-010, PE-011, PE-012. */
@Validated
@RestController
@RequestMapping("/v1/payments")
public class PaymentController {
    private final PaymentWorkflowService workflowService;
    public PaymentController(PaymentWorkflowService workflowService) { this.workflowService = workflowService; }

    @PostMapping("/instant")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public PaymentResponse createInstantPayment(@RequestHeader("Idempotency-Key") @NotBlank String idempotencyKey, @RequestHeader("X-Source-Channel") @NotBlank String sourceChannel, @Valid @RequestBody PaymentInstructionRequest request) {
        return workflowService.createPayment(PaymentRail.SCT_INST, request, idempotencyKey, sourceChannel);
    }

    @PostMapping("/sct")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public PaymentResponse createSctPayment(@RequestHeader("Idempotency-Key") @NotBlank String idempotencyKey, @RequestHeader("X-Source-Channel") @NotBlank String sourceChannel, @Valid @RequestBody PaymentInstructionRequest request) {
        return workflowService.createPayment(PaymentRail.SCT, request, idempotencyKey, sourceChannel);
    }

    @PostMapping("/direct-debit/batches")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public BatchResponse createDirectDebitBatch(@RequestHeader("Idempotency-Key") @NotBlank String idempotencyKey, @RequestHeader("X-Source-Channel") @NotBlank String sourceChannel, @Valid @RequestBody DirectDebitBatchRequest request) {
        return workflowService.createDirectDebitBatch(request, idempotencyKey, sourceChannel);
    }

    @PostMapping("/{paymentId}/settlement")
    public SettlementResponse submitSettlement(@PathVariable String paymentId, @Valid @RequestBody SettlementRequest request) {
        return workflowService.submitSettlement(paymentId, PaymentRail.fromExternal(request.rail()));
    }

    @PostMapping("/exceptions")
    public ExceptionEventResponse handleException(@Valid @RequestBody ExceptionEventRequest request) {
        return workflowService.handleExceptionEvent(request.paymentId(), request.eventType(), request.reasonCode(), request.externalEventId());
    }

    @PostMapping("/reconciliation/run")
    public ReconciliationResponse reconcile(@Valid @RequestBody ReconciliationRequest request) {
        return workflowService.reconcile(request.settlementReportId(), request.businessDate());
    }

    @GetMapping("/reports/summary")
    public ReportingSummaryResponse reportingSummary() {
        return workflowService.reportSummary();
    }
}
