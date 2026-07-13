package com.aif.mfg.paymentengine.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "payment_instructions")
public class PaymentInstruction {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "correlation_id", nullable = false, length = 128)
    private String correlationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_channel", nullable = false, length = 40)
    private SourceChannel sourceChannel;

    @Enumerated(EnumType.STRING)
    @Column(name = "rail", nullable = false, length = 40)
    private PaymentRail rail;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 40)
    private PaymentStatus status;

    @Column(name = "debtor_account_ref", nullable = false, length = 64)
    private String debtorAccountRef;

    @Column(name = "creditor_account_ref", nullable = false, length = 64)
    private String creditorAccountRef;

    @Column(name = "creditor_iban", nullable = false, length = 34)
    private String creditorIban;

    @Column(name = "creditor_bic", length = 11)
    private String creditorBic;

    @Column(name = "amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "requested_execution_date")
    private LocalDate requestedExecutionDate;

    @Column(name = "remittance_information", length = 140)
    private String remittanceInformation;

    @Column(name = "psd2_consent_reference", length = 128)
    private String psd2ConsentReference;

    @Column(name = "sca_authentication_context", length = 128)
    private String scaAuthenticationContext;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected PaymentInstruction() {
    }

    public static PaymentInstruction create(UUID id, String correlationId, SourceChannel sourceChannel, PaymentRail rail, String debtorAccountRef, String creditorAccountRef, String creditorIban, String creditorBic, BigDecimal amount, String currency, LocalDate requestedExecutionDate, String remittanceInformation, String psd2ConsentReference, String scaAuthenticationContext, Instant createdAt) {
        PaymentInstruction instruction = new PaymentInstruction();
        instruction.id = id;
        instruction.correlationId = correlationId;
        instruction.sourceChannel = sourceChannel;
        instruction.rail = rail;
        instruction.status = PaymentStatus.ACCEPTED;
        instruction.debtorAccountRef = debtorAccountRef;
        instruction.creditorAccountRef = creditorAccountRef;
        instruction.creditorIban = creditorIban;
        instruction.creditorBic = creditorBic;
        instruction.amount = amount;
        instruction.currency = currency;
        instruction.requestedExecutionDate = requestedExecutionDate;
        instruction.remittanceInformation = remittanceInformation;
        instruction.psd2ConsentReference = psd2ConsentReference;
        instruction.scaAuthenticationContext = scaAuthenticationContext;
        instruction.createdAt = createdAt;
        return instruction;
    }

    public UUID getId() { return id; }
    public String getCorrelationId() { return correlationId; }
    public SourceChannel getSourceChannel() { return sourceChannel; }
    public PaymentRail getRail() { return rail; }
    public PaymentStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
}
