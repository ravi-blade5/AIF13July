package com.aif.mfg.paymentengine.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ApiExceptionHandlerTest {

    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    @DisplayName("should return RFC 7807 response for domain exception")
    void shouldReturnRfc7807ResponseForDomainException() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/v1/payments");

        PaymentIngressException ex = new PaymentIngressException(HttpStatus.CONFLICT, "IDEMPOTENCY_KEY_CONFLICT", "Conflicting payload");

        var response = handler.handlePaymentIngressException(ex, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        ProblemDetail body = response.getBody();
        assertNotNull(body);
        assertEquals("IDEMPOTENCY_KEY_CONFLICT", body.getTitle());
        assertEquals("IDEMPOTENCY_KEY_CONFLICT", body.getProperties().get("errorCode"));
        assertEquals("/v1/payments", body.getProperties().get("path"));
    }

    @Test
    @DisplayName("should return RFC 7807 response for validation failure")
    void shouldReturnRfc7807ResponseForValidationFailure() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/v1/payments");

        BindException bindException = new BindException(new Object(), "paymentInitiationRequest");
        bindException.addError(new FieldError("paymentInitiationRequest", "creditorIban", "must not be blank"));

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindException.getBindingResult());

        var response = handler.handleValidationException(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ProblemDetail body = response.getBody();
        assertNotNull(body);
        assertEquals("VALIDATION_FAILED", body.getTitle());
        assertEquals("VALIDATION_FAILED", body.getProperties().get("errorCode"));
        assertEquals("/v1/payments", body.getProperties().get("path"));
        assertTrue(body.getProperties().containsKey("errors"));
    }
}
