package com.aif.mfg.paymentengine.api;

import com.aif.mfg.paymentengine.api.dto.MoneyDto;
import com.aif.mfg.paymentengine.api.dto.PaymentInitiationRequest;
import com.aif.mfg.paymentengine.domain.PaymentRail;
import com.aif.mfg.paymentengine.domain.SourceChannel;
import com.aif.mfg.paymentengine.idempotency.IdempotencyRepository;
import com.aif.mfg.paymentengine.testsupport.TestClockConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestClockConfig.class)
class PaymentIngressControllerIT {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("mfg_payment_engine_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);
    }

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private IdempotencyRepository idempotencyRepository;

    @Test
    @DisplayName("should accept payment initiation with supported channel and valid idempotency key")
    void shouldAcceptPaymentInitiationWithSupportedChannelAndValidIdempotencyKey() throws Exception {
        PaymentInitiationRequest request = validRequest();
        mockMvc.perform(post("/v1/payments").contentType(MediaType.APPLICATION_JSON).header("Idempotency-Key", "pay-20260713-0001").header("X-Correlation-Id", "corr-123").content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(header().string("X-Correlation-Id", "corr-123"))
                .andExpect(jsonPath("$.status").value("ACCEPTED"))
                .andExpect(jsonPath("$.rail").value("SCT_INSTANT"))
                .andExpect(jsonPath("$.replayedFromIdempotency").value(false))
                .andExpect(jsonPath("$.traceabilityReference", containsString("AIF-179")));
        assertEquals(1, idempotencyRepository.count());
    }

    @Test
    @DisplayName("should reject request without idempotency key")
    void shouldRejectRequestWithoutIdempotencyKey() throws Exception {
        mockMvc.perform(post("/v1/payments").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("MISSING_IDEMPOTENCY_KEY"))
                .andExpect(jsonPath("$.errorCode").value("MISSING_IDEMPOTENCY_KEY"));
    }

    @Test
    @DisplayName("should reject malformed idempotency key")
    void shouldRejectMalformedIdempotencyKey() throws Exception {
        mockMvc.perform(post("/v1/payments").contentType(MediaType.APPLICATION_JSON).header("Idempotency-Key", "bad key!").content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("INVALID_IDEMPOTENCY_KEY"))
                .andExpect(jsonPath("$.errorCode").value("INVALID_IDEMPOTENCY_KEY"));
    }

    @Test
    @DisplayName("should replay duplicate identical request")
    void shouldReplayDuplicateIdenticalRequest() throws Exception {
        PaymentInitiationRequest request = validRequest();
        mockMvc.perform(post("/v1/payments").contentType(MediaType.APPLICATION_JSON).header("Idempotency-Key", "pay-20260713-0003").content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.replayedFromIdempotency").value(false));
        mockMvc.perform(post("/v1/payments").contentType(MediaType.APPLICATION_JSON).header("Idempotency-Key", "pay-20260713-0003").content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.replayedFromIdempotency").value(true))
                .andExpect(jsonPath("$.paymentInstructionId").exists());
    }

    @Test
    @DisplayName("should reject duplicate key with different payload")
    void shouldRejectDuplicateKeyWithDifferentPayload() throws Exception {
        PaymentInitiationRequest first = validRequest();
        PaymentInitiationRequest second = new PaymentInitiationRequest(SourceChannel.MOBILE, PaymentRail.SCT_INSTANT, "ACC-123456", "BEN-998877", "DE89370400440532013000", "DEUTDEFF", new MoneyDto(new BigDecimal("45.00"), "EUR"), Instant.parse("2026-07-14T00:00:00Z").atZone(ZoneOffset.UTC).toLocalDate(), "Invoice 12345", "consent-abc", "sca-context-xyz");
        mockMvc.perform(post("/v1/payments").contentType(MediaType.APPLICATION_JSON).header("Idempotency-Key", "pay-20260713-0004").content(objectMapper.writeValueAsString(first))).andExpect(status().isAccepted());
        mockMvc.perform(post("/v1/payments").contentType(MediaType.APPLICATION_JSON).header("Idempotency-Key", "pay-20260713-0004").content(objectMapper.writeValueAsString(second)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("IDEMPOTENCY_KEY_CONFLICT"))
                .andExpect(jsonPath("$.errorCode").value("IDEMPOTENCY_KEY_CONFLICT"));
    }

    private PaymentInitiationRequest validRequest() {
        return new PaymentInitiationRequest(SourceChannel.MOBILE, PaymentRail.SCT_INSTANT, "ACC-123456", "BEN-998877", "DE89370400440532013000", "DEUTDEFF", new MoneyDto(new BigDecimal("42.50"), "EUR"), Instant.parse("2026-07-14T00:00:00Z").atZone(ZoneOffset.UTC).toLocalDate(), "Invoice 12345", "consent-abc", "sca-context-xyz");
    }
}
