package com.fund_transfer.backend.dto.Response;

import com.fund_transfer.backend.enums.TransactionStatus;
import lombok.Builder;

import java.math.BigInteger;
import java.time.OffsetDateTime;

/**
 * transactionReference here is OUR system's durable identifier for this
 * transfer (minted server-side, see TransactionService), never the client's
 * idempotencyKey. The two are allowed to diverge — e.g. a client could reuse
 * an idempotency key across accidental duplicate submissions, but every
 * genuinely distinct transfer still gets its own transactionReference.
 *
 * amountMinorUnits is BigInteger paise — see TransferRequest for rationale.
 * completedAt is an OffsetDateTime, which Jackson serializes as a proper
 * ISO-8601 string (e.g. "2026-09-13T10:15:30.123Z") rather than an epoch
 * millis number, so it's human-readable and unambiguous across timezones
 * for any client consuming this API.
 */
@Builder
public record TransactionResponse(
        String transactionReference,
        TransactionStatus status,
        BigInteger amountMinorUnits,
        String failureReason,
        OffsetDateTime completedAt
) {
}