package com.fund_transfer.backend.entity;

import com.fund_transfer.backend.enums.LimitAction;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "global_limit_history",
        indexes = {
                @Index(name = "idx_glob_hist_code", columnList = "limit_code"),
                @Index(name = "idx_glob_hist_time", columnList = "timestamp")
        }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GlobalLimitHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "limit_code", nullable = false, length = 50)
    private String limitCode;

    @Column(name = "per_transaction_limit", precision = 18, scale = 2)
    private BigDecimal perTransactionLimit;

    @Column(name = "daily_limit", precision = 18, scale = 2)
    private BigDecimal dailyLimit;

    @Column(name = "monthly_limit", precision = 18, scale = 2)
    private BigDecimal monthlyLimit;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 30)
    private LimitAction action;

    @Column(name = "performed_by", length = 50)
    private String performedBy;

    @Column(name = "remarks", length = 255)
    private String remarks;

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
