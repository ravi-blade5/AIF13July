package com.aif.mfg.paymentengine.api;

import com.aif.mfg.paymentengine.api.dto.PaymentAcknowledgementResponse;
import com.aif.mfg.paymentengine.api.dto.PaymentInitiationRequest;
import com.aif.mfg.paymentengine.service.PaymentIngressService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping(path = "/v1/payments", produces = MediaType.APPLICATION_JSON_VALUE)
public class PaymentIngressController {

    private final PaymentIngressService paymentIngressService;

    public PaymentIngressController(PaymentIngressService paymentIngressService) {
        this.paymentIngressService = paymentIngressService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PaymentAcknowledgementResponse> initiatePayment(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @Valid @RequestBody PaymentInitiationRequest request
    ) {
        PaymentAcknowledgementResponse response = paymentIngressService.acceptPayment(idempotencyKey, correlationId, request);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .header("X-Correlation-Id", response.correlationId())
                .body(response);
    }
}
