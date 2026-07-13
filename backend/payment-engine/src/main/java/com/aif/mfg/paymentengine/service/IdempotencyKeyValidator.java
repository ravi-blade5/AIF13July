package com.aif.mfg.paymentengine.service;

import com.aif.mfg.paymentengine.exception.PaymentIngressException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class IdempotencyKeyValidator {

    private static final int MAX_LENGTH = 128;
    private static final String SAFE_PATTERN = "^[A-Za-z0-9._:-]{8,128}$";

    public void validate(String idempotencyKey) {
        if (!StringUtils.hasText(idempotencyKey)) {
            throw new PaymentIngressException(HttpStatus.BAD_REQUEST, "MISSING_IDEMPOTENCY_KEY", "State-changing payment initiation requests require an Idempotency-Key header.");
        }
        if (idempotencyKey.length() > MAX_LENGTH || !idempotencyKey.matches(SAFE_PATTERN)) {
            throw new PaymentIngressException(HttpStatus.BAD_REQUEST, "INVALID_IDEMPOTENCY_KEY", "Idempotency-Key must be 8 to 128 characters and contain only letters, digits, '.', '_', ':', or '-'.");
        }
    }
}
