package exceptionHandler;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.OffsetDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        OffsetDateTime timestamp,
        int status,
        String error,        // short machine-friendly code, e.g. "INSUFFICIENT_BALANCE"
        String message,      // human-readable detail
        String path,         // request path that triggered it, e.g. "/api/v1/transfer/sendMoney"
        List<FieldError> errors // only populated for validation failures; null otherwise
) {
    public record FieldError(String field, String message) {
    }
}