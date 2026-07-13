package com.aif.mfg.paymentengine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class PaymentEngineApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentEngineApplication.class, args);
    }
}
