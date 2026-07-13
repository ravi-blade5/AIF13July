package com.aif.mfg.paymentengine.testsupport;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestClockConfig {

    @Bean
    public Clock testClock() {
        return Clock.fixed(Instant.parse("2026-07-13T00:00:00Z"), ZoneOffset.UTC);
    }
}
