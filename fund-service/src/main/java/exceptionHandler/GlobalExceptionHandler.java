package exceptionHandler;


import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // -----------------------------------------------------------------
    // Step 1 failure — request rejected before any money moved.
    // 422 (not 400) since the request is well-formed, it's just
    // semantically invalid given current account state.
    // -----------------------------------------------------------------
    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientBalance(
            InsufficientBalanceException ex, HttpServletRequest request) {
        log.warn("Insufficient balance: {}", ex.getMessage());
        return build(HttpStatus.UNPROCESSABLE_ENTITY, "INSUFFICIENT_BALANCE", ex.getMessage(), request, null);
    }

    // -----------------------------------------------------------------
    // Step 2 failure — CBS definitively rejected the debit (or, per the
    // CbsRestClient notes, we treated a connect-level failure as safe to
    // report as failed). No money moved. 502 signals "the upstream system
    // we depend on failed," which is more accurate than a 4xx here since
    // it isn't the caller's fault.
    // -----------------------------------------------------------------
    @ExceptionHandler(CbsDebitException.class)
    public ResponseEntity<ErrorResponse> handleCbsDebitException(
            CbsDebitException ex, HttpServletRequest request) {
        log.error("CBS debit failed: {}", ex.getMessage(), ex);
        return build(HttpStatus.BAD_GATEWAY, "CBS_DEBIT_FAILED", ex.getMessage(), request, null);
    }

    // -----------------------------------------------------------------
    // Step 6 — the worst case. Debit succeeded, disbursal failed, AND
    // reversal could not be confirmed. This should almost never reach the
    // client as a plain error response in production — by the time you add
    // persistence, this path should also push to a manual-reconciliation
    // queue / alert on-call (see comments in FundTransferService). Kept as
    // a handler here so the API still returns *something* well-formed
    // rather than a raw 500, but 500 status is intentional: this reflects
    // an unresolved internal failure, not a client error.
    // -----------------------------------------------------------------
    @ExceptionHandler(CbsReversalException.class)
    public ResponseEntity<ErrorResponse> handleCbsReversalException(
            CbsReversalException ex, HttpServletRequest request) {
        log.error("CRITICAL — CBS reversal failed/unconfirmed: {}", ex.getMessage(), ex);
        // TODO once persistence/alerting exists: trigger manual reconciliation
        // queue entry and page on-call here, not just log.error.
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "CBS_REVERSAL_UNCONFIRMED", ex.getMessage(), request, null);
    }

    // -----------------------------------------------------------------
    // Bean Validation failures — @Valid @RequestBody rejects the request
    // (e.g. blank ownerCif, amount below @DecimalMin). Collects every
    // field error instead of just the first, so the client can fix
    // everything in one round trip.
    // -----------------------------------------------------------------
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new ErrorResponse.FieldError(fe.getField(), fe.getDefaultMessage()))
                .toList();
        log.warn("Validation failed for request to {}: {}", request.getRequestURI(), fieldErrors);
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "Request validation failed", request, fieldErrors);
    }

    // -----------------------------------------------------------------
    // Malformed JSON body / wrong types in the request.
    // -----------------------------------------------------------------
    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableBody(
            org.springframework.http.converter.HttpMessageNotReadableException ex, HttpServletRequest request) {
        log.warn("Malformed request body at {}: {}", request.getRequestURI(), ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, "MALFORMED_REQUEST", "Request body is missing or malformed", request, null);
    }

    // -----------------------------------------------------------------
    // Wrong HTTP method on a valid path (e.g. GET on /sendMoney).
    // -----------------------------------------------------------------
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        log.warn("Method not allowed at {}: {}", request.getRequestURI(), ex.getMessage());
        return build(HttpStatus.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED", ex.getMessage(), request, null);
    }

    // -----------------------------------------------------------------
    // Catch-all — anything not explicitly handled above. Deliberately
    // generic message to the client (never leak stack traces / internal
    // details in the response body), full detail goes to logs only.
    // -----------------------------------------------------------------
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception at {}", request.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                "An unexpected error occurred. Please try again or contact support.", request, null);
    }

    private ResponseEntity<ErrorResponse> build(
            HttpStatus status, String errorCode, String message,
            HttpServletRequest request, List<ErrorResponse.FieldError> fieldErrors) {
        ErrorResponse body = new ErrorResponse(
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
