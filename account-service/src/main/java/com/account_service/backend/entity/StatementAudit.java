package com.account_service.backend.entity;

import com.account_service.backend.enums.StatementFormat;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(
        name = "statement_audits",
        indexes = {
                @Index(name = "idx_stmt_account", columnList = "account_number"),
                @Index(name = "idx_stmt_ref", columnList = "reference_number")
        }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatementAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "account_number", nullable = false, length = 32)
    private String accountNumber;

    @Column(name = "customer_id", length = 50)
    private String customerId;

    @Column(name = "action_type", nullable = false, length = 20)
    private String actionType;

    @Column(name = "from_date")
    private LocalDate fromDate;

    @Column(name = "to_date")
    private LocalDate toDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "format", length = 10)
    private StatementFormat format;

    @Column(name = "recipient_email", length = 100)
    private String recipientEmail;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "reference_number", nullable = false, length = 64)
    private String referenceNumber;

    @CreatedDate
    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    @PrePersist
    protected void onCreate() {
        if (this.timestamp == null) {
            this.timestamp = Instant.now();
        }
    }
}
