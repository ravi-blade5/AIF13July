package com.mfg.paymentengine.infra;

import com.mfg.paymentengine.config.PaymentEngineProperties;
import com.mfg.paymentengine.domain.PaymentModels.DecisionOutcome;
import com.mfg.paymentengine.domain.PaymentModels.DecisionResult;
import com.mfg.paymentengine.domain.PaymentModels.IsoMessage;
import com.mfg.paymentengine.domain.PaymentModels.LiquidityDecision;
import com.mfg.paymentengine.domain.PaymentModels.PaymentInstruction;
import com.mfg.paymentengine.domain.PaymentModels.PaymentRail;
import com.mfg.paymentengine.domain.PaymentModels.SettlementResult;
import com.mfg.paymentengine.domain.PaymentModels.ValidationResult;
import com.mfg.paymentengine.service.Ports.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.UUID;

/** Corrected default adapters. Traceability: AIF-169..AIF-174 / PE-002..PE-009. */
public final class DefaultComponentAdapters {
    private DefaultComponentAdapters() {}

    @Component
    public static class LoggingDomainEventPublisher implements DomainEventPublisher {
        private static final Logger log = LoggerFactory.getLogger(LoggingDomainEventPublisher.class);
        @Override public void publish(String topic, String correlationId, Object payload) { log.info("event_published topic={} correlation_id={} payload_type={}", topic, correlationId, payload == null ? "null" : payload.getClass().getSimpleName()); }
    }

    @Component
    public static class DefaultValidationEngine implements ValidationEngine {
        @Override public ValidationResult validate(PaymentInstruction instruction) {
            if (!"EUR".equals(instruction.getCurrency())) return ValidationResult.rejected("Only EUR is supported for SEPA rails");
            if (instruction.getAmount() == null || instruction.getAmount().compareTo(BigDecimal.ZERO) <= 0) return ValidationResult.rejected("Amount must be greater than zero");
            if (!isLikelyIban(instruction.getDebtorIban()) || !isLikelyIban(instruction.getCreditorIban())) return ValidationResult.rejected("Invalid IBAN format");
            return ValidationResult.ok();
        }
        private boolean isLikelyIban(String iban) { return iban != null && iban.length() >= 15 && iban.length() <= 34 && iban.matches("[A-Z]{2}[0-9A-Z]+"); }
    }

    @Component
    public static class DefaultSanctionsScreening implements SanctionsScreening {
        private final PaymentEngineProperties properties;
        public DefaultSanctionsScreening(PaymentEngineProperties properties) { this.properties = properties; }
        @Override public DecisionResult screen(PaymentInstruction instruction) {
            String name = instruction.getCreditorName() == null ? "" : instruction.getCreditorName().toUpperCase(Locale.ROOT);
            boolean blocked = properties.screening().blockedNameTokens().stream().map(token -> token.toUpperCase(Locale.ROOT)).anyMatch(name::contains);
            return blocked ? new DecisionResult(DecisionOutcome.BLOCK, "Potential sanctions/watchlist match", 1000) : new DecisionResult(DecisionOutcome.ALLOW, "No screening match", 0);
        }
    }

    @Component
    public static class DefaultFraudScoring implements FraudScoring {
        private final PaymentEngineProperties properties;
        public DefaultFraudScoring(PaymentEngineProperties properties) { this.properties = properties; }
        @Override public DecisionResult score(PaymentInstruction instruction) {
            int score = calculateDeterministicScore(instruction);
            if (score >= properties.fraud().blockThreshold()) return new DecisionResult(DecisionOutcome.BLOCK, "Fraud score exceeds block threshold", score);
            if (score >= properties.fraud().flagThreshold()) return new DecisionResult(DecisionOutcome.FLAG, "Fraud score exceeds flag threshold", score);
            return new DecisionResult(DecisionOutcome.ALLOW, "Fraud score allowed", score);
        }
        private int calculateDeterministicScore(PaymentInstruction instruction) { int score = 100; if (instruction.getAmount() != null && instruction.getAmount().compareTo(new BigDecimal("10000")) > 0) score += 500; if (instruction.getCreditorName() != null && instruction.getCreditorName().toUpperCase(Locale.ROOT).contains("RISK")) score += 350; return score; }
    }

    @Component
    public static class DefaultIso20022Broker implements Iso20022Broker {
        @Override public IsoMessage prepare(PaymentInstruction instruction) {
            String messageType = switch (instruction.getRail()) { case SCT, SCT_INST -> "pacs.008"; case SDD_CORE, SDD_B2B -> "pacs.003"; };
            String payload = """
                    <Document>
                      <MessageType>%s</MessageType>
                      <PaymentId>%s</PaymentId>
                      <CorrelationId>%s</CorrelationId>
                      <DebtorIban>%s</DebtorIban>
                      <CreditorIban>%s</CreditorIban>
                      <Amount currency=\"%s\">%s</Amount>
                      <RemittanceInformation>%s</RemittanceInformation>
                    </Document>
                    """.formatted(escape(messageType), escape(instruction.getPaymentId()), escape(instruction.getCorrelationId()), escape(instruction.getDebtorIban()), escape(instruction.getCreditorIban()), escape(instruction.getCurrency()), instruction.getAmount(), escape(instruction.getRemittanceInformation()));
            return new IsoMessage(messageType, payload);
        }
        private String escape(String value) { if (value == null) return ""; return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&apos;"); }
    }

    @Component
    public static class DefaultMandateManagement implements MandateManagement {
        @Override public boolean isMandateValid(String mandateReference, String debtorIban, String creditorIban) { return mandateReference != null && mandateReference.length() >= 6 && debtorIban != null && creditorIban != null && !mandateReference.toUpperCase(Locale.ROOT).contains("INVALID"); }
    }

    @Component
    public static class DefaultLiquidityManager implements LiquidityManager {
        private final PaymentEngineProperties properties;
        public DefaultLiquidityManager(PaymentEngineProperties properties) { this.properties = properties; }
        @Override public LiquidityDecision check(PaymentInstruction instruction) { if (instruction.getAmount() == null) return new LiquidityDecision(false, "Missing amount"); if (instruction.getAmount().compareTo(properties.liquidity().defaultLimitEur()) > 0) return new LiquidityDecision(false, "Insufficient liquidity for configured limit"); return new LiquidityDecision(true, "Liquidity approved"); }
    }

    @Component
    public static class DefaultSettlementAdapter implements SettlementAdapter {
        @Override public SettlementResult submit(PaymentInstruction instruction, PaymentRail rail) { String route = switch (rail) { case SCT_INST -> "INSTANT_CSM"; case SCT -> "PAN_EUROPEAN_CSM"; case SDD_CORE, SDD_B2B -> "PAN_EUROPEAN_CSM_SDD"; }; return new SettlementResult(route, "ACSP", "ack_" + UUID.randomUUID()); }
    }
}
