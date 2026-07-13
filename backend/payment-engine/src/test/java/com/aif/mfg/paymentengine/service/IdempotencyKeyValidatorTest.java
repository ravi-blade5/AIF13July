package com.aif.mfg.paymentengine.service;

import com.aif.mfg.paymentengine.exception.PaymentIngressException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;

class IdempotencyKeyValidatorTest {

    private final IdempotencyKeyValidator validator = new IdempotencyKeyValidator();

    @Test
    @DisplayName("should reject null idempotency key")
    void shouldRejectNullIdempotencyKey() {
        PaymentIngressException ex = assertThrows(PaymentIngressException.class, () -> validator.validate(null));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getHttpStatus());
        assertEquals("MISSING_IDEMPOTENCY_KEY", ex.getErrorCode());
    }

    @Test
    @DisplayName("should reject blank idempotency key")
    void shouldRejectBlankIdempotencyKey() {
        PaymentIngressException ex = assertThrows(PaymentIngressException.class, () -> validator.validate("   "));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getHttpStatus());
        assertEquals("MISSING_IDEMPOTENCY_KEY", ex.getErrorCode());
    }

    @Test
    @DisplayName("should reject malformed idempotency key")
    void shouldRejectMalformedIdempotencyKey() {
        PaymentIngressException ex = assertThrows(PaymentIngressException.class, () -> validator.validate("bad key!"));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getHttpStatus());
        assertEquals("INVALID_IDEMPOTENCY_KEY", ex.getErrorCode());
    }

    @Test
    @DisplayName("should reject too short idempotency key")
    void shouldRejectTooShortIdempotencyKey() {
        PaymentIngressException ex = assertThrows(PaymentIngressException.class, () -> validator.validate("short"));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getHttpStatus());
        assertEquals("INVALID_IDEMPOTENCY_KEY", ex.getErrorCode());
    }

    @Test
    @DisplayName("should accept valid idempotency key")
    void shouldAcceptValidIdempotencyKey() {
        assertDoesNotThrow(() -> validator.validate("pay-20260713-0001"));
    }
}
