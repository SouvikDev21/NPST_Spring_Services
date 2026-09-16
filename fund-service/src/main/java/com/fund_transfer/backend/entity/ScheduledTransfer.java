package com.fund_transfer.backend.entity;

import com.fund_transfer.backend.enums.ScheduleFrequency;
import com.fund_transfer.backend.enums.ScheduleStatus;
import com.fund_transfer.backend.enums.TransferMode;
import jakarta.persistence.*;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(
        name = "scheduled_transfer",
        indexes = {
                @Index(name = "idx_scheduled_transfer_cif", columnList = "cif"),
                @Index(name = "idx_scheduled_transfer_next_execution", columnList = "next_execution_date, status")
        }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduledTransfer {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, length = 20)
    private String cif;

    @Column(name = "keycloak_user_id", nullable = false)
    private String keycloakUserId;

    @Column(name = "initiator_account_number", nullable = false, length = 34)
    private String initiatorAccountNumber; // owner's account debited on each execution — feeds TransferRequest.initiatorAccountNumber

    @Column(name = "beneficiary_id", nullable = false)
    private Long beneficiaryId;

    @Column(name = "amount_minor_units", nullable = false)
    private BigInteger amountMinorUnits;

    @Enumerated(EnumType.STRING)
    @Column(name = "transfer_mode", nullable = false, length = 20)
    private TransferMode transferMode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ScheduleFrequency frequency;

    @Column(name = "next_execution_date", nullable = false)
    private LocalDate nextExecutionDate;

    @Column(name = "end_date")
    private LocalDate endDate; // nullable — open-ended recurring transfers are valid

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ScheduleStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "last_execution_status", length = 20)
    private ScheduleStatus lastExecutionStatus; // see scheduler note: TransactionStatus is richer, mapped down on write

    @Column(name = "last_executed_at")
    private Instant lastExecutedAt;

    @Column(name = "retry_count", nullable = false)
    @Builder.Default
    private Long retryCount = 0L;

    @Column(name = "max_retries", nullable = false)
    @Builder.Default
    private Long maxRetries = 3L;

    @Version
    private Long version; // ScheduledTransferSchedulerJob writes here on every execution attempt

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;
}