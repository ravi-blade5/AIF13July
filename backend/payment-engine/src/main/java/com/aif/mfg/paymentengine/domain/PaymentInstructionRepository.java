package com.aif.mfg.paymentengine.domain;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentInstructionRepository extends JpaRepository<PaymentInstruction, UUID> {
}
