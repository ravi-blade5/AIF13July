package com.aif.mfg.paymentengine.idempotency;

import java.time.Clock;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class IdempotencyCleanupJob {

    private static final Logger log = LoggerFactory.getLogger(IdempotencyCleanupJob.class);

    private final IdempotencyRepository idempotencyRepository;
    private final Clock clock;

    public IdempotencyCleanupJob(IdempotencyRepository idempotencyRepository, Clock clock) {
        this.idempotencyRepository = idempotencyRepository;
        this.clock = clock;
    }

    @Transactional
    @Scheduled(fixedDelayString = "${mfg.payment-engine.idempotency.cleanup-fixed-delay-ms:900000}")
    public void deleteExpiredRecords() {
        int deleted = idempotencyRepository.deleteAllExpired(Instant.now(clock));
        if (deleted > 0) {
            log.info("Deleted {} expired idempotency records", deleted);
        }
    }
}
