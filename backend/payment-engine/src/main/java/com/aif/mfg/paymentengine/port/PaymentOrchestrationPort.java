package com.aif.mfg.paymentengine.port;

import com.aif.mfg.paymentengine.domain.PaymentInstruction;

public interface PaymentOrchestrationPort {

    void notifyInstructionAccepted(PaymentInstruction paymentInstruction);
}
