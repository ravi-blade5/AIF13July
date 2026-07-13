package com.aif.mfg.paymentengine.service;

import com.aif.mfg.paymentengine.api.dto.PaymentAcknowledgementResponse;
import com.aif.mfg.paymentengine.api.dto.PaymentInitiationRequest;
import com.aif.mfg.paymentengine.domain.PaymentInstruction;
import com.aif.mfg.paymentengine.domain.PaymentInstructionRepository;
import com.aif.mfg.paymentengine.exception.PaymentIngressException;
import com.aif.mfg.paymentengine.idempotency.IdempotencyRecord;
import com.aif.mfg.paymentengine.idempotency.IdempotencyRepository;
import com.aif.mfg.paymentengine.port.PaymentOrchestrationPort;
import com.aif.mfg.paymentengine.port.PaymentValidationPort;
import com.aif.mfg.paymentengine.traceability.Traceability;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class PaymentIngressService {

    private static final Logger log = LoggerFactory.getLogger(PaymentIngressService.class);
    private static final Duration IDEMPOTENCY_TTL = Duration.ofHours(24);

    private final PaymentInstructionRepository paymentInstructionRepository;
    private final IdempotencyRepository idempotencyRepository;
    private final PaymentInstructionMapper paymentInstructionMapper;
    private final IdempotencyKeyValidator idempotencyKeyValidator;
    private final SourceChannelPolicy sourceChannelPolicy;
    private final PaymentValidationPort paymentValidationPort;
    private final PaymentOrchestrationPort paymentOrchestrationPort;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public PaymentIngressService(PaymentInstructionRepository paymentInstructionRepository, IdempotencyRepository idempotencyRepository, PaymentInstructionMapper paymentInstructionMapper, IdempotencyKeyValidator idempotencyKeyValidator, SourceChannelPolicy sourceChannelPolicy, PaymentValidationPort paymentValidationPort, PaymentOrchestrationPort paymentOrchestrationPort, ObjectMapper objectMapper, Clock clock) {
        this.paymentInstructionRepository = paymentInstructionRepository;
        this.idempotencyRepository = idempotencyRepository;
        this.paymentInstructionMapper = paymentInstructionMapper;
        this.idempotencyKeyValidator = idempotencyKeyValidator;
        this.sourceChannelPolicy = sourceChannelPolicy;
        this.paymentValidationPort = paymentValidationPort;
        this.paymentOrchestrationPort = paymentOrchestrationPort;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    @Transactional
    public PaymentAcknowledgementResponse acceptPayment(String idempotencyKey, String correlationId, PaymentInitiationRequest request) {
        idempotencyKeyValidator.validate(idempotencyKey);
        sourceChannelPolicy.validateSupported(request.sourceChannel());

        String effectiveCorrelationId = StringUtils.hasText(correlationId) ? correlationId : UUID.randomUUID().toString();
        Instant now = Instant.now(clock);
        String requestHash = hashRequest(request);

        idempotencyRepository.deleteExpiredByIdempotencyKey(idempotencyKey, now);

        return idempotencyRepository.findActiveByIdempotencyKey(idempotencyKey, now)
                .map(existing -> handleExistingRecord(existing, requestHash))
                .orElseGet(() -> createInstructionAndIdempotencyRecord(idempotencyKey, requestHash, effectiveCorrelationId, request, now));
    }

    private PaymentAcknowledgementResponse handleExistingRecord(IdempotencyRecord existing, String requestHash) {
        if (!existing.getRequestHash().equals(requestHash)) {
            throw new PaymentIngressException(HttpStatus.CONFLICT, "IDEMPOTENCY_KEY_CONFLICT", "The supplied Idempotency-Key has already been used for a different request payload within the 24-hour deduplication window.");
        }
        PaymentAcknowledgementResponse original = deserializeResponse(existing.getResponseBody());
        log.info("Returning idempotent replay for paymentInstructionId={} traceability={}", existing.getPaymentInstructionId(), Traceability.PE001_SLICE);
        return new PaymentAcknowledgementResponse(original.paymentInstructionId(), original.correlationId(), original.status(), original.rail(), original.acceptedAt(), true, original.traceabilityReference());
    }

    private PaymentAcknowledgementResponse createInstructionAndIdempotencyRecord(String idempotencyKey, String requestHash, String correlationId, PaymentInitiationRequest request, Instant now) {
        PaymentInstruction instruction = paymentInstructionMapper.toInstruction(correlationId, request);
        PaymentInstruction saved = paymentInstructionRepository.save(instruction);

        paymentValidationPort.submitForValidation(saved);
        paymentOrchestrationPort.notifyInstructionAccepted(saved);

        PaymentAcknowledgementResponse response = new PaymentAcknowledgementResponse(saved.getId(), saved.getCorrelationId(), saved.getStatus(), saved.getRail(), saved.getCreatedAt(), false, Traceability.PE001_SLICE);
        String responseJson = serializeResponse(response);

        try {
            idempotencyRepository.saveAndFlush(IdempotencyRecord.completed(idempotencyKey, requestHash, saved.getId(), responseJson, now, now.plus(IDEMPOTENCY_TTL)));
        } catch (DataIntegrityViolationException raceCondition) {
            log.warn("Idempotency insert race detected. Reloading active record.");
            IdempotencyRecord existing = idempotencyRepository.findActiveByIdempotencyKey(idempotencyKey, Instant.now(clock)).orElseThrow(() -> raceCondition);
            return handleExistingRecord(existing, requestHash);
        }

        log.info("Accepted payment instruction id={} rail={} sourceChannel={} traceability={}", saved.getId(), saved.getRail(), saved.getSourceChannel(), Traceability.PE001_SLICE);
        return response;
    }

    private String hashRequest(PaymentInitiationRequest request) {
        try {
            String canonicalJson = objectMapper.writeValueAsString(request);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(canonicalJson.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (JsonProcessingException ex) {
            throw new PaymentIngressException(HttpStatus.BAD_REQUEST, "REQUEST_HASHING_FAILED", "Payment request could not be canonicalized for idempotency processing.");
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 algorithm is not available", ex);
        }
    }

    private String serializeResponse(PaymentAcknowledgementResponse response) {
        try {
            return objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Unable to serialize idempotency response body", ex);
        }
    }

    private PaymentAcknowledgementResponse deserializeResponse(String responseBody) {
        try {
            return objectMapper.readValue(responseBody, PaymentAcknowledgementResponse.class);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Unable to deserialize stored idempotency response body", ex);
        }
    }
}
