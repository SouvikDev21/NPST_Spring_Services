package com.fund_transfer.backend.dto.Request;

import java.math.BigDecimal;
import java.math.BigInteger;

public record CbsReversalRequest(
        String originalDebitReference,
        BigInteger amount,
        String requestId // idempotency key for the reversal call itself
) {
}
