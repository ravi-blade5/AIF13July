package com.aif.mfg.paymentengine.adapter;

import com.aif.mfg.paymentengine.domain.PaymentInstruction;
import com.aif.mfg.paymentengine.port.PaymentOrchestrationPort;
import com.aif.mfg.paymentengine.traceability.Traceability;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class NoOpPaymentOrchestrationAdapter implements PaymentOrchestrationPort {

    private static final Logger log = LoggerFactory.getLogger(NoOpPaymentOrchestrationAdapter.class);

    @Override
    public void notifyInstructionAccepted(PaymentInstruction paymentInstruction) {
        log.info("PE-005 orchestration placeholder notified for paymentInstructionId={} traceability={}", paymentInstruction.getId(), Traceability.PE005_PLACEHOLDER);
    }
}
