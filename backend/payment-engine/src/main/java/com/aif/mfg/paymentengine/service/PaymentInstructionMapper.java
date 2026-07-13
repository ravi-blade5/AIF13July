package com.aif.mfg.paymentengine.service;

import com.aif.mfg.paymentengine.api.dto.PaymentInitiationRequest;
import com.aif.mfg.paymentengine.domain.PaymentInstruction;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class PaymentInstructionMapper {

    private final Clock clock;

    public PaymentInstructionMapper(Clock clock) {
        this.clock = clock;
    }

    public PaymentInstruction toInstruction(String correlationId, PaymentInitiationRequest request) {
        Instant now = Instant.now(clock);
        return PaymentInstruction.create(UUID.randomUUID(), correlationId, request.sourceChannel(), request.rail(), request.debtorAccountRef(), request.creditorAccountRef(), request.creditorIban(), request.creditorBic(), request.instructedAmount().amount(), request.instructedAmount().currency(), request.requestedExecutionDate(), request.remittanceInformation(), request.psd2ConsentReference(), request.scaAuthenticationContext(), now);
    }
}
