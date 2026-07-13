package com.mfg.paymentengine.integration;

import com.mfg.paymentengine.api.PaymentController;
import com.mfg.paymentengine.api.dto.PaymentDtos.*;
import com.mfg.paymentengine.config.PaymentEngineProperties;
import com.mfg.paymentengine.domain.PaymentModels.PaymentRail;
import com.mfg.paymentengine.domain.PaymentModels.PaymentStatus;
import com.mfg.paymentengine.infra.DefaultComponentAdapters;
import com.mfg.paymentengine.infra.InMemoryAdapters;
import com.mfg.paymentengine.service.PaymentWorkflowService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

/** Integration-style tests. Traceability: F-001,F-002,F-003 / AC-001..AC-025. */
class PaymentWorkflowIntegrationTest {
    private PaymentController controller;
    @BeforeEach void setUp() { PaymentEngineProperties p = props(); PaymentWorkflowService service = new PaymentWorkflowService(new InMemoryAdapters.InMemoryPaymentRepository(), new InMemoryAdapters.InMemoryBatchRepository(), new InMemoryAdapters.InMemoryIdempotencyStore(p), new DefaultComponentAdapters.LoggingDomainEventPublisher(), new DefaultComponentAdapters.DefaultValidationEngine(), new DefaultComponentAdapters.DefaultSanctionsScreening(p), new DefaultComponentAdapters.DefaultFraudScoring(p), new DefaultComponentAdapters.DefaultIso20022Broker(), new DefaultComponentAdapters.DefaultMandateManagement(), new DefaultComponentAdapters.DefaultLiquidityManager(p), new DefaultComponentAdapters.DefaultSettlementAdapter(), p); controller = new PaymentController(service); }
    @Test @DisplayName("Instant payment end-to-end happy path") void instantPaymentEndToEndHappyPath() { var response = controller.createInstantPayment("idem-int-1", "MOBILE", validPayment()); assertThat(response.status()).isEqualTo(PaymentStatus.MESSAGE_READY.name()); assertThat(response.rail()).isEqualTo(PaymentRail.SCT_INST.name()); assertThat(response.traceability()).containsEntry("source_doc_id", "MFG-ARC-PAY-CORE-001"); }
    @Test @DisplayName("Direct debit batch partial acceptance integration") void directDebitBatchPartialAcceptanceIntegration() { var response = controller.createDirectDebitBatch("idem-batch-1", "CORPORATE", validBatch()); assertThat(response.status()).isEqualTo("PARTIALLY_ACCEPTED"); assertThat(response.acceptedCount()).isEqualTo(1); assertThat(response.rejectedCount()).isEqualTo(1); }
    @Test @DisplayName("Settlement integration path should update lifecycle") void settlementEndpointIntegrationPathShouldUpdateLifecycle() { var created = controller.createInstantPayment("idem-int-3", "MOBILE", validPayment()); var settlement = controller.submitSettlement(created.paymentId(), new SettlementRequest("SCT_INST")); assertThat(settlement.settlementStatus()).isEqualTo("ACSP"); assertThat(settlement.settlementRoute()).isEqualTo("INSTANT_CSM"); }
    @Test @DisplayName("Reconciliation and reporting integration path should return results") void reconciliationAndReportingIntegrationPathShouldReturnResults() { controller.createInstantPayment("idem-int-5", "MOBILE", validPayment()); var reconciliation = controller.reconcile(new ReconciliationRequest("report-1", LocalDate.now())); ReportingSummaryResponse summary = controller.reportingSummary(); assertThat(reconciliation.status()).isEqualTo("COMPLETED"); assertThat(summary.totalPayments()).isGreaterThanOrEqualTo(1); }
    @Test @DisplayName("Exception integration path should process supported return event") void exceptionIntegrationPathShouldProcessSupportedReturnEvent() { var created = controller.createInstantPayment("idem-int-6", "MOBILE", validPayment()); var response = controller.handleException(new ExceptionEventRequest(created.paymentId(), "RETURN", "AC01", "ext-1")); assertThat(response.status()).isEqualTo("PROCESSED"); assertThat(response.eventType()).isEqualTo("RETURN"); }
    private PaymentInstructionRequest validPayment() { return new PaymentInstructionRequest("DE89370400440532013000", "FR1420041010050500013M02606", "Example Merchant", new BigDecimal("25.50"), "EUR", "Invoice 123"); }
    private DirectDebitBatchRequest validBatch() { return new DirectDebitBatchRequest("corp-1", "batch-ref-1", LocalDate.now().plusDays(1), List.of(new DirectDebitInstruction("instr-1", "MANDATE123", "DE89370400440532013000", "FR1420041010050500013M02606", "Debtor One", new BigDecimal("15.00"), "EUR"), new DirectDebitInstruction("instr-2", "INVALID-MANDATE", "DE89370400440532013000", "FR1420041010050500013M02606", "Debtor Two", new BigDecimal("25.00"), "EUR"))); }
    private PaymentEngineProperties props() { return new PaymentEngineProperties(new PaymentEngineProperties.Security(false, List.of("MOBILE", "WEB", "CORPORATE", "PSD2", "TPP", "B2B")), new PaymentEngineProperties.Idempotency(24), new PaymentEngineProperties.Screening(List.of("SANCTIONED", "BLOCKED")), new PaymentEngineProperties.Fraud(900, 700), new PaymentEngineProperties.Liquidity(new BigDecimal("10000000")), new PaymentEngineProperties.Traceability("MFG-ARC-PAY-CORE-001", "v1.0", "1783915119965/MFG-Architecture-6page.docx")); }
}
