package com.aif.mfg.paymentengine.service;

import com.aif.mfg.paymentengine.domain.SourceChannel;
import com.aif.mfg.paymentengine.exception.PaymentIngressException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;

class SourceChannelPolicyTest {

    private final SourceChannelPolicy policy = new SourceChannelPolicy();

    @Test
    @DisplayName("should accept all supported source channels")
    void shouldAcceptAllSupportedSourceChannels() {
        for (SourceChannel channel : SourceChannel.values()) {
            assertDoesNotThrow(() -> policy.validateSupported(channel));
        }
    }

    @Test
    @DisplayName("should reject null source channel")
    void shouldRejectNullSourceChannel() {
        PaymentIngressException ex = assertThrows(PaymentIngressException.class, () -> policy.validateSupported(null));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getHttpStatus());
        assertEquals("UNSUPPORTED_SOURCE_CHANNEL", ex.getErrorCode());
    }
}
