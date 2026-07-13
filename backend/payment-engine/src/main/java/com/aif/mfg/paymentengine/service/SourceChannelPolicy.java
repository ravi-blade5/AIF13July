package com.aif.mfg.paymentengine.service;

import com.aif.mfg.paymentengine.domain.SourceChannel;
import com.aif.mfg.paymentengine.exception.PaymentIngressException;
import java.util.EnumSet;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class SourceChannelPolicy {

    private static final Set<SourceChannel> SUPPORTED_CHANNELS = EnumSet.allOf(SourceChannel.class);

    public void validateSupported(SourceChannel sourceChannel) {
        if (sourceChannel == null || !SUPPORTED_CHANNELS.contains(sourceChannel)) {
            throw new PaymentIngressException(HttpStatus.BAD_REQUEST, "UNSUPPORTED_SOURCE_CHANNEL", "Payment initiation source channel is not supported by PE-001.");
        }
    }
}
