package com.term_deposit.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "interest_accrual_log")
@Data
public class InterestAccrualLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    // Links to the specific Fixed Deposit account
    @Column(name = "term_deposit_id", nullable = false)
    private UUID termDepositId;

    // The date this interest was calculated for (e.g., 2026-09-15)
    @Column(name = "accrual_date", nullable = false)
    private LocalDate accrualDate;

    // The exact amount of money generated today
    @Column(name = "accrued_amount", precision = 19, scale = 4, nullable = false)
    private BigDecimal accruedAmount;

    // Audit timestamp for when the system actually ran the job
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

}