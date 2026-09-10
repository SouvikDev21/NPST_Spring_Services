package com.fund_transfer.backend.ifsc;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ifsc")
public class IfscController {

    private final IfscLookupService ifscLookupService;

    public IfscController(IfscLookupService ifscLookupService) {
        this.ifscLookupService = ifscLookupService;
    }

    @GetMapping("/{ifscCode}")
    public ResponseEntity<IfscDetailsResponse> getBranchDetails(@PathVariable String ifscCode) {
        return ResponseEntity.ok(ifscLookupService.lookup(ifscCode));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleInvalidFormat(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(new ErrorResponse("INVALID_IFSC_FORMAT", e.getMessage()));
    }

    @ExceptionHandler(IfscNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(IfscNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("IFSC_NOT_FOUND", e.getMessage()));
    }

    @ExceptionHandler(IfscLookupUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleUpstreamUnavailable(IfscLookupUnavailableException e) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ErrorResponse("IFSC_LOOKUP_UNAVAILABLE", e.getMessage()));
    }

    public record ErrorResponse(String code, String message) {
    }
}
