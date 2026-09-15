package com.term_deposit.backend.entity;

import com.term_deposit.backend.enums.*;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "term_deposit")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TermDeposit {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(unique = true, nullable = false)
    private String depositNumber;
    private String cif;
    private UUID keycloakUserId;
    private UUID productId;
    @Enumerated(EnumType.STRING)
    private DepositType depositType;
    private String fundingAccountReference;
    private BigInteger principalMinorUnits;
    private BigDecimal interestRate;
    private Integer tenureDays;
    private LocalDate startDate;
    private LocalDate maturityDate;
    private BigInteger projectedInterestMinorUnits;
    private BigInteger maturityAmountMinorUnits;
    @Enumerated(EnumType.STRING)
    private InterestPayoutFrequency interestPayoutFrequency;
    @Enumerated(EnumType.STRING)
    private MaturityInstruction maturityInstruction;
    private BigDecimal prematureRate;
    private BigInteger prematurePenaltyMinorUnits;
    private BigInteger estimatedPrematurePayoutMinorUnits;
    private BigInteger interestEarnedMinorUnits;
    private boolean tdsApplicable;
    @Enumerated(EnumType.STRING)
    private TdsStatus tdsStatus;
    private String cbsReferenceNumber;
    @Enumerated(EnumType.STRING)
    private DepositStatus status;
    private String bankCode;
    @Version
    private Long version;
    private Instant initiatedAt;
    private Instant activatedAt;
    private Instant maturedAt;
    private Instant closedAt;
    @CreationTimestamp
    private Instant createdAt;
    @UpdateTimestamp
    private Instant updatedAt;
}