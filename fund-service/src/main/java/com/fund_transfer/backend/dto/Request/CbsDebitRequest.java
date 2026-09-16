package com.fund_transfer.backend.dto.Request;

import java.math.BigDecimal;
import java.math.BigInteger;

public record CbsDebitRequest(
        String accountNumber,
        BigInteger amount,
        // Field name CBS expects for the idempotency key varies by vendor —
        // common names: "referenceId", "requestId", "clientRefNo". Confirm with docs.
        String requestId
) {
}