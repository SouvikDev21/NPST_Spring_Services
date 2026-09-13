package com.fund_transfer.backend.Cbs;


import java.math.BigDecimal;
import java.math.BigInteger;

public interface CbsClient {

    /**
     * Step 1 helper — read current available balance for the owner account.
     * Used for the pre-check before attempting a debit.
     */
    BigDecimal getAvailableBalance(String ownerAccountNumber);

    /**
     * Step 2 — debit the owner's account for the transfer amount.
     * Must be called with the SAME idempotencyKey on any retry, so CBS
     * (or our own dedup layer, if CBS doesn't support idempotency keys
     * natively) can recognize and safely no-op a duplicate call.
     *
     * @return a CBS-issued debit reference id, used later for reversal if needed
     * @throws com.fund_transfer.backend.exception.CbsDebitException on a
     *         definite rejection (e.g. insufficient funds detected at CBS,
     *         account frozen). On a network timeout, the caller is
     *         responsible for querying debit status rather than assuming
     *         failure — see TransactionService.
     */
    String debit( String ownerAccountNumber, BigDecimal amount, String idempotencyKey);

    /**
     * Step 5 — compensating transaction: credit the amount back to the owner
     * because disbursal (NPCI) failed after the debit succeeded.
     * Must also be idempotent — pass the ORIGINAL debitReference so CBS can
     * link the reversal to the original debit and avoid a double reversal.
     *
     * @throws com.fund_transfer.backend.exception.CbsReversalException if the
     *         reversal itself cannot be confirmed — this must escalate to
     *         manual reconciliation, never fail silently.
     */
    void reverseDebit(String debitReference, BigInteger amount, String idempotencyKey);
}
