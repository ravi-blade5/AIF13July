package com.aif.mfg.paymentengine.api.dto;

import com.aif.mfg.paymentengine.domain.PaymentRail;
import com.aif.mfg.paymentengine.domain.PaymentStatus;
import java.time.Instant;
import java.util.UUID;

public record PaymentAcknowledgementResponse(
        UUID paymentInstructionId,
        String correlationId,
        PaymentStatus status,
        PaymentRail rail,
        Instant acceptedAt,
        boolean replayedFromIdempotency,
        String traceabilityReference
) {
}
