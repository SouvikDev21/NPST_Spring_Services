package com.fund_transfer.backend.Npci;

import java.math.BigDecimal;
import java.math.BigInteger;

public interface NpciClient {

    /**
     * Step 3 — send the disbursal request to NPCI to credit the beneficiary.
     * Returns true if NPCI confirms success, false if NPCI confirms failure.
     * Should throw (not return false) on ambiguous outcomes like a timeout,
     * so the caller can distinguish "definitely failed" from "unknown" —
     * those two cases should NOT both trigger an automatic reversal blindly.
     */
    boolean disburse(String beneficiaryAccountNumber, String ifscCode, BigInteger amount, String idempotencyKey);
}
