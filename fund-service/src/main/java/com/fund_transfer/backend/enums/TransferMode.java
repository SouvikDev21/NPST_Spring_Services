package com.fund_transfer.backend.enums;

/**
 * NOTE: this enum was referenced by Beneficiary/CreateBeneficiaryRequest/BeneficiaryResponse
 * but not supplied — created from the payment rails named in PRD Section 6.4 / 7.2
 * (Intra-bank, IMPS, NEFT/RTGS). Confirm naming/values match what the team intends
 * before merging, especially whether NEFT and RTGS should be separate modes or
 * collapsed into one "INTER_BANK" mode with the rail chosen at transfer time.
 */
public enum TransferMode {
    INTRA_BANK,
    IMPS,
    NEFT,
    RTGS
}
