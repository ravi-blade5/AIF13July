package com.aif.mfg.paymentengine.service;

import com.aif.mfg.paymentengine.api.dto.MoneyDto;
import com.aif.mfg.paymentengine.api.dto.PaymentAcknowledgementResponse;
import com.aif.mfg.paymentengine.api.dto.PaymentInitiationRequest;
import com.aif.mfg.paymentengine.domain.PaymentInstruction;
import com.aif.mfg.paymentengine.domain.PaymentInstructionRepository;
import com.aif.mfg.paymentengine.domain.PaymentRail;
import com.aif.mfg.paymentengine.domain.PaymentStatus;
import com.aif.mfg.paymentengine.domain.SourceChannel;
import com.aif.mfg.paymentengine.exception.PaymentIngressException;
import com.aif.mfg.paymentengine.idempotency.IdempotencyRecord;
import com.aif.mfg.paymentengine.idempotency.IdempotencyRepository;
import com.aif.mfg.paymentengine.port.PaymentOrchestrationPort;
import com.aif.mfg.paymentengine.port.PaymentValidationPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentIngressServiceTest {

    @Mock private PaymentInstructionRepository paymentInstructionRepository;
    @Mock private IdempotencyRepository idempotencyRepository;
    @Mock private PaymentInstructionMapper paymentInstructionMapper;
    @Mock private IdempotencyKeyValidator idempotencyKeyValidator;
    @Mock private SourceChannelPolicy sourceChannelPolicy;
    @Mock private PaymentValidationPort paymentValidationPort;
    @Mock private PaymentOrchestrationPort paymentOrchestrationPort;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private final Clock clock = Clock.fixed(Instant.parse("2026-07-13T00:00:00Z"), ZoneOffset.UTC);
    private PaymentIngressService paymentIngressService;
    private PaymentInitiationRequest request;
    private PaymentInstruction paymentInstruction;

    @BeforeEach
    void setUp() {
        paymentIngressService = new PaymentIngressService(paymentInstructionRepository, idempotencyRepository, paymentInstructionMapper, idempotencyKeyValidator, sourceChannelPolicy, paymentValidationPort, paymentOrchestrationPort, objectMapper, clock);
        request = new PaymentInitiationRequest(SourceChannel.MOBILE, PaymentRail.SCT_INSTANT, "ACC-123456", "BEN-998877", "DE89370400440532013000", "DEUTDEFF", new MoneyDto(new BigDecimal("42.50"), "EUR"), Instant.parse("2026-07-14T00:00:00Z").atZone(ZoneOffset.UTC).toLocalDate(), "Invoice 12345", "consent-abc", "sca-context-xyz");
        paymentInstruction = PaymentInstruction.create(UUID.fromString("00000000-0000-0000-0000-000000000111"), "corr-123", SourceChannel.MOBILE, PaymentRail.SCT_INSTANT, "ACC-123456", "BEN-998877", "DE89370400440532013000", "DEUTDEFF", new BigDecimal("42.50"), "EUR", Instant.parse("2026-07-14T00:00:00Z").atZone(ZoneOffset.UTC).toLocalDate(), "Invoice 12345", "consent-abc", "sca-context-xyz", Instant.parse("2026-07-13T00:00:00Z"));
    }

    @Test
    @DisplayName("should accept and persist first-time payment instruction")
    void shouldAcceptAndPersistFirstTimePaymentInstruction() {
        when(paymentInstructionMapper.toInstruction(eq("corr-123"), eq(request))).thenReturn(paymentInstruction);
        when(paymentInstructionRepository.save(paymentInstruction)).thenReturn(paymentInstruction);
        when(idempotencyRepository.findActiveByIdempotencyKey(eq("pay-20260713-0001"), any())).thenReturn(Optional.empty());
        PaymentAcknowledgementResponse response = paymentIngressService.acceptPayment("pay-20260713-0001", "corr-123", request);
        assertEquals(paymentInstruction.getId(), response.paymentInstructionId());
        assertEquals("corr-123", response.correlationId());
        assertEquals(PaymentStatus.ACCEPTED, response.status());
        assertEquals(PaymentRail.SCT_INSTANT, response.rail());
        assertFalse(response.replayedFromIdempotency());
        verify(paymentValidationPort).submitForValidation(paymentInstruction);
        verify(paymentOrchestrationPort).notifyInstructionAccepted(paymentInstruction);
        verify(idempotencyRepository).saveAndFlush(any(IdempotencyRecord.class));
    }

    @Test
    @DisplayName("should reject conflicting payload under same idempotency key")
    void shouldRejectConflictingPayloadUnderSameIdempotencyKey() throws Exception {
        PaymentAcknowledgementResponse originalResponse = new PaymentAcknowledgementResponse(paymentInstruction.getId(), "corr-123", PaymentStatus.ACCEPTED, PaymentRail.SCT_INSTANT, paymentInstruction.getCreatedAt(), false, "traceability");
        IdempotencyRecord existing = IdempotencyRecord.completed("pay-20260713-0001", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", paymentInstruction.getId(), objectMapper.writeValueAsString(originalResponse), Instant.parse("2026-07-13T00:00:00Z"), Instant.parse("2026-07-14T00:00:00Z"));
        when(idempotencyRepository.findActiveByIdempotencyKey(eq("pay-20260713-0001"), any())).thenReturn(Optional.of(existing));
        PaymentIngressException ex = assertThrows(PaymentIngressException.class, () -> paymentIngressService.acceptPayment("pay-20260713-0001", "corr-123", request));
        assertEquals(HttpStatus.CONFLICT, ex.getHttpStatus());
        assertEquals("IDEMPOTENCY_KEY_CONFLICT", ex.getErrorCode());
        verify(paymentInstructionRepository, never()).save(any());
    }

    @Test
    @DisplayName("should clean expired idempotency record before accepting reused key")
    void shouldCleanExpiredIdempotencyRecordBeforeAcceptingReusedKey() {
        when(paymentInstructionMapper.toInstruction(eq("corr-123"), eq(request))).thenReturn(paymentInstruction);
        when(paymentInstructionRepository.save(paymentInstruction)).thenReturn(paymentInstruction);
        when(idempotencyRepository.findActiveByIdempotencyKey(eq("pay-20260713-0001"), any())).thenReturn(Optional.empty());
        PaymentAcknowledgementResponse response = paymentIngressService.acceptPayment("pay-20260713-0001", "corr-123", request);
        assertNotNull(response);
        verify(idempotencyRepository).deleteExpiredByIdempotencyKey(eq("pay-20260713-0001"), any());
    }

    @Test
    @DisplayName("should invoke validation and orchestration ports after acceptance")
    void shouldInvokeValidationAndOrchestrationPortsAfterAcceptance() {
        when(paymentInstructionMapper.toInstruction(eq("corr-123"), eq(request))).thenReturn(paymentInstruction);
        when(paymentInstructionRepository.save(paymentInstruction)).thenReturn(paymentInstruction);
        when(idempotencyRepository.findActiveByIdempotencyKey(eq("pay-20260713-0001"), any())).thenReturn(Optional.empty());
        paymentIngressService.acceptPayment("pay-20260713-0001", "corr-123", request);
        verify(paymentValidationPort).submitForValidation(paymentInstruction);
        verify(paymentOrchestrationPort).notifyInstructionAccepted(paymentInstruction);
    }
}
