package com.fund_transfer.backend.enums;

public enum TransactionStatus {
    INITIATED,          // row created, balance validated, nothing debited yet
    DEBITED,            // CBS debit succeeded, NPCI disbursal not yet attempted
    SUCCESS,            // CBS debit + NPCI disbursal both succeeded
    FAILED,             // rejected before any money moved (e.g. insufficient balance, CBS pre-debit rejection)
    NPCI_FAILED,        // debit succeeded, NPCI failed, reversal succeeded — owner refunded
    REVERSAL_FAILED     // debit succeeded, NPCI failed, reversal ALSO failed — needs manual reconciliation
}
