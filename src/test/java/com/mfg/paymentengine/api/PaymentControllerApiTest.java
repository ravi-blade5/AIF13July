package com.mfg.paymentengine.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mfg.paymentengine.api.dto.PaymentDtos.*;
import com.mfg.paymentengine.config.PaymentEngineProperties;
import com.mfg.paymentengine.infra.DefaultComponentAdapters;
import com.mfg.paymentengine.infra.InMemoryAdapters;
import com.mfg.paymentengine.service.PaymentWorkflowService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** API tests. Traceability: INT-001, INT-002, INT-010 / AC-001..AC-025. */
class PaymentControllerApiTest {
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    @Test @DisplayName("POST /v1/payments/instant should reject missing idempotency header") void instantShouldRejectMissingIdempotencyHeader() throws Exception { buildMockMvc().perform(post("/v1/payments/instant").contentType(MediaType.APPLICATION_JSON).header("X-Source-Channel", "MOBILE").content(objectMapper.writeValueAsString(validPayment()))).andExpect(status().isBadRequest()).andExpect(jsonPath("$.title").exists()).andExpect(jsonPath("$.source_doc_id").value("MFG-ARC-PAY-CORE-001")); }
    @Test @DisplayName("POST /v1/payments/instant should accept valid request") void instantShouldAcceptValidRequest() throws Exception { buildMockMvc().perform(post("/v1/payments/instant").contentType(MediaType.APPLICATION_JSON).header("Idempotency-Key", "idem-api-1").header("X-Source-Channel", "MOBILE").content(objectMapper.writeValueAsString(validPayment()))).andExpect(status().isAccepted()); }
    @Test @DisplayName("POST /v1/payments/instant should reject unsupported source") void instantShouldRejectUnsupportedSourceChannel() throws Exception { buildMockMvc().perform(post("/v1/payments/instant").contentType(MediaType.APPLICATION_JSON).header("Idempotency-Key", "idem-api-3").header("X-Source-Channel", "UNAPPROVED").content(objectMapper.writeValueAsString(validPayment()))).andExpect(status().isBadRequest()).andExpect(jsonPath("$.title").exists()); }
    @Test @DisplayName("POST /v1/payments/direct-debit/batches should accept valid batch") void batchShouldAcceptValidBatch() throws Exception { buildMockMvc().perform(post("/v1/payments/direct-debit/batches").contentType(MediaType.APPLICATION_JSON).header("Idempotency-Key", "batch-api-1").header("X-Source-Channel", "CORPORATE").content(objectMapper.writeValueAsString(validBatch()))).andExpect(status().isAccepted()); }
    @Test @DisplayName("POST settlement should accept settlement request") void settlementShouldAcceptSettlementRequest() throws Exception { MockMvc mockMvc = buildMockMvc(); String created = mockMvc.perform(post("/v1/payments/instant").contentType(MediaType.APPLICATION_JSON).header("Idempotency-Key", "idem-api-4").header("X-Source-Channel", "MOBILE").content(objectMapper.writeValueAsString(validPayment()))).andReturn().getResponse().getContentAsString(); JsonNode json = objectMapper.readTree(created); mockMvc.perform(post("/v1/payments/" + json.get("paymentId").asText() + "/settlement").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(new SettlementRequest("SCT_INST")))).andExpect(status().isOk()); }
    @Test @DisplayName("GET /v1/payments/reports/summary should return summary") void reportSummaryShouldReturnSummary() throws Exception { buildMockMvc().perform(get("/v1/payments/reports/summary")).andExpect(status().isOk()).andExpect(jsonPath("$.totalPayments").exists()).andExpect(jsonPath("$.traceability.source_doc_id").value("MFG-ARC-PAY-CORE-001")); }
    private MockMvc buildMockMvc() { PaymentEngineProperties p = props(); PaymentWorkflowService service = new PaymentWorkflowService(new InMemoryAdapters.InMemoryPaymentRepository(), new InMemoryAdapters.InMemoryBatchRepository(), new InMemoryAdapters.InMemoryIdempotencyStore(p), new DefaultComponentAdapters.LoggingDomainEventPublisher(), new DefaultComponentAdapters.DefaultValidationEngine(), new DefaultComponentAdapters.DefaultSanctionsScreening(p), new DefaultComponentAdapters.DefaultFraudScoring(p), new DefaultComponentAdapters.DefaultIso20022Broker(), new DefaultComponentAdapters.DefaultMandateManagement(), new DefaultComponentAdapters.DefaultLiquidityManager(p), new DefaultComponentAdapters.DefaultSettlementAdapter(), p); return MockMvcBuilders.standaloneSetup(new PaymentController(service)).setControllerAdvice(new com.mfg.paymentengine.error.ApiExceptionHandler()).build(); }
    private PaymentInstructionRequest validPayment() { return new PaymentInstructionRequest("DE89370400440532013000", "FR1420041010050500013M02606", "Example Merchant", new BigDecimal("25.50"), "EUR", "Invoice 123"); }
    private DirectDebitBatchRequest validBatch() { return new DirectDebitBatchRequest("corp-1", "batch-ref-1", LocalDate.now().plusDays(1), List.of(new DirectDebitInstruction("instr-1", "MANDATE123", "DE89370400440532013000", "FR1420041010050500013M02606", "Debtor One", new BigDecimal("15.00"), "EUR"))); }
    private PaymentEngineProperties props() { return new PaymentEngineProperties(new PaymentEngineProperties.Security(false, List.of("MOBILE", "WEB", "CORPORATE", "PSD2", "TPP", "B2B")), new PaymentEngineProperties.Idempotency(24), new PaymentEngineProperties.Screening(List.of("SANCTIONED", "BLOCKED")), new PaymentEngineProperties.Fraud(900, 700), new PaymentEngineProperties.Liquidity(new BigDecimal("10000000")), new PaymentEngineProperties.Traceability("MFG-ARC-PAY-CORE-001", "v1.0", "1783915119965/MFG-Architecture-6page.docx")); }
}
