package com.aif.mfg.paymentengine.api.dto;

import com.aif.mfg.paymentengine.domain.PaymentRail;
import com.aif.mfg.paymentengine.domain.SourceChannel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record PaymentInitiationRequest(
        @NotNull
        SourceChannel sourceChannel,

        @NotNull
        PaymentRail rail,

        @NotBlank
        @Size(max = 64)
        String debtorAccountRef,

        @NotBlank
        @Size(max = 64)
        String creditorAccountRef,

        @NotBlank
        @Pattern(regexp = "^[A-Z]{2}[0-9A-Z]{13,32}$", message = "creditorIban must be a valid IBAN-like value")
        String creditorIban,

        @Size(max = 11)
        String creditorBic,

        @NotNull
        @Valid
        MoneyDto instructedAmount,

        LocalDate requestedExecutionDate,

        @Size(max = 140)
        String remittanceInformation,

        @Size(max = 128)
        String psd2ConsentReference,

        @Size(max = 128)
        String scaAuthenticationContext
) {
}
