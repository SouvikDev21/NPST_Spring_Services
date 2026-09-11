package com.fund_transfer.backend.dto.Response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CbsReversalResponse(
        String status, // "SUCCESS" / "FAILED"
        String reversalReference
) {
}
