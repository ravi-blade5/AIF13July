package com.aif.mfg.paymentengine.exception;

import org.springframework.http.HttpStatus;

public class PaymentIngressException extends RuntimeException {

    private final HttpStatus httpStatus;
    private final String errorCode;

    public PaymentIngressException(HttpStatus httpStatus, String errorCode, String message) {
        super(message);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
    }

    public HttpStatus getHttpStatus() { return httpStatus; }
    public String getErrorCode() { return errorCode; }
}
