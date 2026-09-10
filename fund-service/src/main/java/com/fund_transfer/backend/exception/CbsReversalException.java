package com.fund_transfer.backend.exception;

// Thrown in Step 5 if the compensating reversal itself fails.
// This is the worst case: money left the owner's account (CBS debit succeeded),
// disbursal failed (NPCI failed), and now we can't reverse it either.
// This must NOT be silently swallowed — it needs to alert ops / go to a manual
// reconciliation queue, because the customer has been debited with no beneficiary
// credit and no refund.
public class CbsReversalException extends RuntimeException {
    public CbsReversalException(String message, Throwable cause) {
        super(message, cause);
    }
}
