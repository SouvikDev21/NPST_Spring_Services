package com.term_deposit.backend.entity;

import com.term_deposit.backend.enums.RateStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "term_deposit_product_rate")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TermDepositProductRate {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private UUID productId;
    private Integer minTenureDays;
    private Integer maxTenureDays;
    private BigDecimal interestRate;
    private String customerCategory;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    @Enumerated(EnumType.STRING)
    private RateStatus status;
    private String bankCode;
    @Version
    private Long version;
    @CreationTimestamp
    private Instant createdAt;
    @UpdateTimestamp
    private Instant updatedAt;
}