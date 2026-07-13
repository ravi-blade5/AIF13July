package com.aif.mfg.paymentengine.adapter;

import com.aif.mfg.paymentengine.domain.PaymentInstruction;
import com.aif.mfg.paymentengine.port.PaymentValidationPort;
import com.aif.mfg.paymentengine.traceability.Traceability;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class NoOpPaymentValidationAdapter implements PaymentValidationPort {

    private static final Logger log = LoggerFactory.getLogger(NoOpPaymentValidationAdapter.class);

    @Override
    public void submitForValidation(PaymentInstruction paymentInstruction) {
        log.info("PE-002 validation placeholder invoked for paymentInstructionId={} traceability={}", paymentInstruction.getId(), Traceability.PE002_PLACEHOLDER);
    }
}
