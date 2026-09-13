package com.fund_transfer.backend.dto.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigInteger;

/**
 * Trimmed request body for a fund transfer.
 *
 * Deliberately excludes:
 *  - idempotencyKey        -> moved to the "Idempotency-Key" HTTP header.
 *                             It's transport/retry metadata, not business data,
 *                             and keeping it out of the body means it can never
 *                             accidentally get persisted as part of the transfer
 *                             record or logged alongside PII in the wrong place.
 *  - beneficiaryBankName   -> derivable server-side from beneficiaryIfsc via an
 *                             IFSC master lookup. Never trust a client-supplied
 *                             bank name; resolve it yourself and treat a mismatch
 *                             (if you ever DO accept it for display) as a red flag.
 *
 * amountMinorUnits is BigInteger, representing PAISE (1 rupee = 100 paise).
 * BigInteger over Long removes any silent overflow ceiling and keeps this an
 * exact integer count with zero risk of decimal/rounding drift — never do
 * arithmetic on this value directly; convert to rupees (BigDecimal) at the
 * point of any calculation or display, see MoneyUtil.paiseToRupees().
 */
public record TransferRequest(

        @NotBlank(message = "initiatorAccountNumber is required")
        String initiatorAccountNumber,

        @NotBlank(message = "destinationAccountNumber is required")
        String destinationAccountNumber,

        @NotBlank(message = "beneficiaryIfsc is required")
        String beneficiaryIfsc,

        @NotNull(message = "amountMinorUnits is required")
        BigInteger amountMinorUnits
) {
        public TransferRequest {
                if (amountMinorUnits != null && amountMinorUnits.signum() <= 0) {
                        throw new IllegalArgumentException("amountMinorUnits must be a positive integer number of paise");
                }
        }
}