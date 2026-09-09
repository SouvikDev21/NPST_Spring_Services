package com.fund_transfer.backend.entity;

import com.fund_transfer.backend.enums.LimitStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "global_limits",
        indexes = {
                @Index(name = "idx_global_limit_code", columnList = "limit_code", unique = true),
                @Index(name = "idx_global_limit_status", columnList = "status")
        }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GlobalLimit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "limit_code", nullable = false, unique = true, length = 50)
    private String limitCode;

    @Column(name = "limit_name", nullable = false, length = 100)
    private String limitName;

    @Column(name = "per_transaction_limit", nullable = false, precision = 18, scale = 2)
    private BigDecimal perTransactionLimit;

    @Column(name = "daily_limit", nullable = false, precision = 18, scale = 2)
    private BigDecimal dailyLimit;

    @Column(name = "monthly_limit", nullable = false, precision = 18, scale = 2)
    private BigDecimal monthlyLimit;

    @Column(name = "cooling_period_hours", nullable = false)
    private Integer coolingPeriodHours;

    @Column(name = "currency", nullable = false, length = 10)
    private String currency;

    @Column(name = "description", length = 255)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private LimitStatus status;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
        if (this.updatedAt == null) {
            this.updatedAt = Instant.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
