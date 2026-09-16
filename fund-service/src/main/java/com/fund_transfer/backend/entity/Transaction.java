package com.fund_transfer.backend.entity;

import com.fund_transfer.backend.enums.TransactionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigInteger;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

/**
 * Durable record of a transfer's saga state.
 *
 * idempotencyKey has a UNIQUE constraint — this is the real safety net
 * against a concurrent duplicate request, not the findByIdempotencyKey()
 * check in the service (that check has a race window between read and
 * insert; the DB constraint is what actually prevents two rows for the
 * same key under concurrent load — catch the DataIntegrityViolationException
 * on save() as a fallback signal that this was a race-losing duplicate).
 */
@Entity
@Table(
        name = "transactions",
        uniqueConstraints = @UniqueConstraint(name = "uq_idempotency_key", columnNames = "idempotency_key")
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    // UUID primary key instead of an auto-increment Long. Generated in
    // Java (see @PrePersist below) rather than DB-generated, so the id is
    // known immediately on the entity even before the INSERT runs — useful
    // if you need to log/reference it before the transaction commits.
    // transactionReference stays as a separate human/business-facing
    // identifier (see class docs) distinct from this internal PK.
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    // Our own durable identifier — see TransactionService for generation.
    @Column(name = "transaction_reference", nullable = false, unique = true, length = 64)
    private String transactionReference;

    // Client-supplied retry-deduplication key, from the Idempotency-Key header.
    @Column(name = "idempotency_key", nullable = false, length = 128)
    private String idempotencyKey;

    @Column(name = "initiator_account_number", nullable = false, length = 34)
    private String initiatorAccountNumber;

    @Column(name = "destination_account_number", nullable = false, length = 34)
    private String destinationAccountNumber;

    @Column(name = "beneficiary_ifsc", nullable = false, length = 11)
    private String beneficiaryIfsc;

    // Stored as NUMERIC(20,0) in the DB — BigInteger paise, never a decimal
    // rupee value. Map explicitly to avoid Hibernate inferring a scale.
    @Column(name = "amount_minor_units", nullable = false, precision = 20, scale = 0)
    private BigInteger amountMinorUnits;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TransactionStatus status;

    // Reference returned by CBS for the debit call — needed for reversal.
    @Column(name = "cbs_debit_reference", length = 64)
    private String cbsDebitReference;

    @Column(name = "failure_reason", length = 512)
    private String failureReason;

    // OffsetDateTime, not Instant — persisted and serialized as a proper
    // ISO-8601 string with an explicit offset (we always write it in UTC,
    // "Z"), rather than a raw epoch/system timestamp. Easier to read
    // directly out of the DB and unambiguous for any downstream consumer.
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    void onCreate() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }
}