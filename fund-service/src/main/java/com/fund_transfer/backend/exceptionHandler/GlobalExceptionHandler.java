package com.fund_transfer.backend.exceptionHandler;


import com.fund_transfer.backend.exception.CbsDebitException;
import com.fund_transfer.backend.exception.CbsReversalException;
import com.fund_transfer.backend.exception.InsufficientBalanceException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientBalance(
            InsufficientBalanceException ex, HttpServletRequest request) {
        log.warn("Insufficient balance: {}", ex.getMessage());
        return build(HttpStatus.UNPROCESSABLE_ENTITY, "INSUFFICIENT_BALANCE", ex.getMessage(), request, null);
    }


    @ExceptionHandler(CbsDebitException.class)
    public ResponseEntity<ErrorResponse> handleCbsDebitException(
            CbsDebitException ex, HttpServletRequest request) {
        log.error("CBS debit failed: {}", ex.getMessage(), ex);
        return build(HttpStatus.BAD_GATEWAY, "CBS_DEBIT_FAILED", ex.getMessage(), request, null);
    }


    @ExceptionHandler(CbsReversalException.class)
    public ResponseEntity<ErrorResponse> handleCbsReversalException(
            CbsReversalException ex, HttpServletRequest request) {
        log.error("CRITICAL — CBS reversal failed/unconfirmed: {}", ex.getMessage(), ex);
        // TODO once persistence/alerting exists: trigger manual reconciliation
        // queue entry and page on-call here, not just log.error.
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "CBS_REVERSAL_UNCONFIRMED", ex.getMessage(), request, null);
    }

    // -----------------------------------------------------------------
    // OTP was not sent (OTP service unreachable/rejected the send call).
    // 502 — the failure is upstream (OTP service), not the caller's fault.
    // -----------------------------------------------------------------
    @ExceptionHandler(OtpSendException.class)
    public ResponseEntity<exceptionHandler.ErrorResponse> handleOtpSendException(
            OtpSendException ex, HttpServletRequest request) {
        log.warn("OTP send failed at {}: {}", request.getRequestURI(), ex.getMessage());
        return build(HttpStatus.BAD_GATEWAY, "OTP_SEND_FAILED", ex.getMessage(), request, null);
    }

    // -----------------------------------------------------------------
    // OTP verification failed — wrong code, expired reference, mismatched
    // cif, or the OTP service being unreachable. 401: the caller has not
    // proven they're the account holder for this action, regardless of
    // which Keycloak-authenticated user they otherwise are.
    // -----------------------------------------------------------------
    @ExceptionHandler(OtpVerificationException.class)
    public ResponseEntity<exceptionHandler.ErrorResponse> handleOtpVerificationException(
            OtpVerificationException ex, HttpServletRequest request) {
        log.warn("OTP verification failed at {}: {}", request.getRequestURI(), ex.getMessage());
        return build(HttpStatus.UNAUTHORIZED, "OTP_VERIFICATION_FAILED", ex.getMessage(), request, null);
    }

    // -----------------------------------------------------------------
    // Required "X-CIF" header missing from a beneficiary-management call.
    // -----------------------------------------------------------------
    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<exceptionHandler.ErrorResponse> handleMissingHeader(
            MissingRequestHeaderException ex, HttpServletRequest request) {
        log.warn("Missing required header at {}: {}", request.getRequestURI(), ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, "MISSING_HEADER",
                "Required header '" + ex.getHeaderName() + "' is missing", request, null);
    }

    // -----------------------------------------------------------------
    // Bean Validation failures — @Valid @RequestBody rejects the request
    // (e.g. blank ownerCif, amount below @DecimalMin). Collects every
    // field error instead of just the first, so the client can fix
    // everything in one round trip.
    // -----------------------------------------------------------------
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<exceptionHandler.ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new exceptionHandler.ErrorResponse.FieldError(fe.getField(), fe.getDefaultMessage()))
                .toList();
        log.warn("Validation failed for request to {}: {}", request.getRequestURI(), fieldErrors);
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "Request validation failed", request, fieldErrors);
    }

    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<exceptionHandler.ErrorResponse> handleUnreadableBody(
            org.springframework.http.converter.HttpMessageNotReadableException ex, HttpServletRequest request) {
        log.warn("Malformed request body at {}: {}", request.getRequestURI(), ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, "MALFORMED_REQUEST", "Request body is missing or malformed", request, null);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<exceptionHandler.ErrorResponse> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        log.warn("Method not allowed at {}: {}", request.getRequestURI(), ex.getMessage());
        return build(HttpStatus.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED", ex.getMessage(), request, null);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<exceptionHandler.ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception at {}", request.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                "An unexpected error occurred. Please try again or contact support.", request, null);
    }

    private ResponseEntity<exceptionHandler.ErrorResponse> build(
            HttpStatus status, String errorCode, String message,
            HttpServletRequest request, List<exceptionHandler.ErrorResponse.FieldError> fieldErrors) {
        exceptionHandler.ErrorResponse body = new exceptionHandler.ErrorResponse(
                OffsetDateTime.now(),
                status.value(),
                errorCode,
                message,
                request.getRequestURI(),
                fieldErrors
        );
        return ResponseEntity.status(status).body(body);
    }
}