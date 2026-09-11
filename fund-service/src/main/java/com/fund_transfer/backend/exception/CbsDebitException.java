package com.fund_transfer.backend.exception;

// Thrown in Step 2 — CBS rejected or timed out on the debit call.
// Caller must treat this as "money did NOT move" only if the CBS response is
// unambiguous. On a timeout, do NOT assume failure — see FundTransferService
// and CbsRestClient notes on distinguishing a rejection from a timeout.
public class CbsDebitException extends RuntimeException {
    public CbsDebitException(String message) {
        super(message);
    }

    public CbsDebitException(String message, Throwable cause) {
        super(message, cause);
    }
}
