package com.mfg.paymentengine.error;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;

/** RFC 7807 handler. Traceability: MFG-ARC-PAY-CORE-001 v1.0 / AC-002,AC-006,AC-009,AC-015,AC-021. */
@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(DomainValidationException.class)
    ResponseEntity<ProblemDetail> handleValidation(DomainValidationException ex, HttpServletRequest request) { return problem(HttpStatus.BAD_REQUEST, ex.code(), ex.getMessage(), request.getRequestURI()); }
    @ExceptionHandler(DomainConflictException.class)
    ResponseEntity<ProblemDetail> handleConflict(DomainConflictException ex, HttpServletRequest request) { return problem(HttpStatus.CONFLICT, ex.code(), ex.getMessage(), request.getRequestURI()); }
    @ExceptionHandler(DomainNotFoundException.class)
    ResponseEntity<ProblemDetail> handleNotFound(DomainNotFoundException ex, HttpServletRequest request) { return problem(HttpStatus.NOT_FOUND, ex.code(), ex.getMessage(), request.getRequestURI()); }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ProblemDetail> handleBeanValidation(MethodArgumentNotValidException ex, HttpServletRequest request) { String detail = ex.getBindingResult().getFieldErrors().stream().findFirst().map(error -> error.getField() + " " + error.getDefaultMessage()).orElse("Invalid request"); return problem(HttpStatus.BAD_REQUEST, "REQUEST_VALIDATION_ERROR", detail, request.getRequestURI()); }
    @ExceptionHandler({ConstraintViolationException.class, MissingRequestHeaderException.class})
    ResponseEntity<ProblemDetail> handleConstraint(Exception ex, HttpServletRequest request) { return problem(HttpStatus.BAD_REQUEST, "REQUEST_VALIDATION_ERROR", ex.getMessage(), request.getRequestURI()); }
    @ExceptionHandler(Exception.class)
    ResponseEntity<ProblemDetail> handleUnexpected(Exception ex, HttpServletRequest request) { return problem(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Unexpected payment engine error", request.getRequestURI()); }

    private ResponseEntity<ProblemDetail> problem(HttpStatus status, String code, String detail, String path) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setType(URI.create("https://mfg.example/problems/" + code));
        problem.setTitle(code);
        problem.setInstance(URI.create(path));
        problem.setProperty("timestamp", Instant.now().toString());
        problem.setProperty("source_doc_id", "MFG-ARC-PAY-CORE-001");
        problem.setProperty("source_doc_version", "v1.0");
        return ResponseEntity.status(status).body(problem);
    }

    public abstract static class CodedDomainException extends RuntimeException {
        private final String code;
        protected CodedDomainException(String code, String message) { super(message); this.code = code; }
        public String code() { return code; }
    }
    public static class DomainValidationException extends CodedDomainException { public DomainValidationException(String code, String message) { super(code, message); } }
    public static class DomainConflictException extends CodedDomainException { public DomainConflictException(String code, String message) { super(code, message); } }
    public static class DomainNotFoundException extends CodedDomainException { public DomainNotFoundException(String code, String message) { super(code, message); } }
}
