package com.fund_transfer.backend.dto.Response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CbsDebitResponse(
        String status,          // e.g. "SUCCESS" / "FAILED" / "PENDING" — confirm exact values with CBS docs
        String debitReference,  // CBS's own reference id, needed later for reversal
        String reasonCode,      // present on failure, e.g. "INSUFFICIENT_FUNDS", "ACCOUNT_FROZEN"
        String reasonMessage
) {
}