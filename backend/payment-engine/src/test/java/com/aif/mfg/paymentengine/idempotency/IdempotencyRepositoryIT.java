package com.aif.mfg.paymentengine.idempotency;

import com.aif.mfg.paymentengine.api.dto.PaymentAcknowledgementResponse;
import com.aif.mfg.paymentengine.domain.PaymentInstruction;
import com.aif.mfg.paymentengine.domain.PaymentInstructionRepository;
import com.aif.mfg.paymentengine.domain.PaymentRail;
import com.aif.mfg.paymentengine.domain.PaymentStatus;
import com.aif.mfg.paymentengine.domain.SourceChannel;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
class IdempotencyRepositoryIT {

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

    @Autowired private IdempotencyRepository idempotencyRepository;
    @Autowired private PaymentInstructionRepository paymentInstructionRepository;
    @Autowired private ObjectMapper objectMapper;

    @Test
    @DisplayName("should persist and retrieve active idempotency record")
    void shouldPersistAndRetrieveActiveIdempotencyRecord() throws Exception {
        PaymentInstruction instruction = paymentInstructionRepository.save(PaymentInstruction.create(UUID.randomUUID(), "corr-001", SourceChannel.MOBILE, PaymentRail.SCT_INSTANT, "ACC-123456", "BEN-998877", "DE89370400440532013000", "DEUTDEFF", new BigDecimal("42.50"), "EUR", Instant.parse("2026-07-14T00:00:00Z").atZone(java.time.ZoneOffset.UTC).toLocalDate(), "Invoice 12345", "consent-abc", "sca-context-xyz", Instant.parse("2026-07-13T00:00:00Z")));
        PaymentAcknowledgementResponse response = new PaymentAcknowledgementResponse(instruction.getId(), "corr-001", PaymentStatus.ACCEPTED, PaymentRail.SCT_INSTANT, instruction.getCreatedAt(), false, "traceability-ref");
        IdempotencyRecord record = IdempotencyRecord.completed("pay-20260713-0001", "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef", instruction.getId(), objectMapper.writeValueAsString(response), Instant.parse("2026-07-13T00:00:00Z"), Instant.parse("2026-07-14T00:00:00Z"));
        idempotencyRepository.saveAndFlush(record);
        Optional<IdempotencyRecord> loaded = idempotencyRepository.findActiveByIdempotencyKey("pay-20260713-0001", Instant.parse("2026-07-13T12:00:00Z"));
        assertTrue(loaded.isPresent());
        assertEquals("pay-20260713-0001", loaded.get().getIdempotencyKey());
    }

    @Test
    @DisplayName("should delete expired records")
    void shouldDeleteExpiredRecords() {
        PaymentInstruction instruction = paymentInstructionRepository.save(PaymentInstruction.create(UUID.randomUUID(), "corr-003", SourceChannel.MOBILE, PaymentRail.SCT_INSTANT, "ACC-123456", "BEN-998877", "DE89370400440532013000", "DEUTDEFF", new BigDecimal("42.50"), "EUR", Instant.parse("2026-07-14T00:00:00Z").atZone(java.time.ZoneOffset.UTC).toLocalDate(), "Invoice 12345", "consent-abc", "sca-context-xyz", Instant.parse("2026-07-13T00:00:00Z")));
        IdempotencyRecord record = IdempotencyRecord.completed("pay-20260713-0003", "cccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc", instruction.getId(), "{}", Instant.parse("2026-07-13T00:00:00Z"), Instant.parse("2026-07-13T01:00:00Z"));
        idempotencyRepository.saveAndFlush(record);
        int deleted = idempotencyRepository.deleteAllExpired(Instant.parse("2026-07-13T02:00:00Z"));
        assertEquals(1, deleted);
        assertTrue(idempotencyRepository.findActiveByIdempotencyKey("pay-20260713-0003", Instant.parse("2026-07-13T02:00:00Z")).isEmpty());
    }
}
