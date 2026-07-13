package com.mfg.paymentengine.config;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.List;

@Validated
@ConfigurationProperties(prefix = "payment-engine")
public record PaymentEngineProperties(
        Security security,
        Idempotency idempotency,
        Screening screening,
        Fraud fraud,
        Liquidity liquidity,
        Traceability traceability
) {
    public record Security(boolean requireAuthentication, List<@NotBlank String> allowedSourceChannels) {}
    public record Idempotency(@Min(1) long ttlHours) {}
    public record Screening(List<@NotBlank String> blockedNameTokens) {}
    public record Fraud(@Min(1) int blockThreshold, @Min(1) int flagThreshold) {}
    public record Liquidity(@DecimalMin("0.01") BigDecimal defaultLimitEur) {}
    public record Traceability(@NotBlank String sourceDocId, @NotBlank String sourceDocVersion, @NotBlank String sourceFile) {}
}
