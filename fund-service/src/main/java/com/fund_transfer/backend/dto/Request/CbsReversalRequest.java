package com.fund_transfer.backend.dto.Request;

import java.math.BigDecimal;

public record CbsReversalRequest(
        String originalDebitReference,
        BigDecimal amount,
        String requestId // idempotency key for the reversal call itself
) {
}
