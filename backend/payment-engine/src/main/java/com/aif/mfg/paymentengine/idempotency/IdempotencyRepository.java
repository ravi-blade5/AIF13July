package com.aif.mfg.paymentengine.idempotency;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IdempotencyRepository extends JpaRepository<IdempotencyRecord, UUID> {

    @Query("""
            select r
            from IdempotencyRecord r
            where r.idempotencyKey = :idempotencyKey
              and r.expiresAt > :now
            """)
    Optional<IdempotencyRecord> findActiveByIdempotencyKey(@Param("idempotencyKey") String idempotencyKey, @Param("now") Instant now);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            delete from IdempotencyRecord r
            where r.idempotencyKey = :idempotencyKey
              and r.expiresAt <= :now
            """)
    int deleteExpiredByIdempotencyKey(@Param("idempotencyKey") String idempotencyKey, @Param("now") Instant now);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            delete from IdempotencyRecord r
            where r.expiresAt <= :now
            """)
    int deleteAllExpired(@Param("now") Instant now);
}
