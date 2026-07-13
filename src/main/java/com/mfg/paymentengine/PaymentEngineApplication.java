package com.mfg.paymentengine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Traceability: MFG-ARC-PAY-CORE-001 v1.0 / PE-001..PE-012 / AIF-161..AIF-174.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class PaymentEngineApplication {
    public static void main(String[] args) {
        SpringApplication.run(PaymentEngineApplication.class, args);
    }
}
