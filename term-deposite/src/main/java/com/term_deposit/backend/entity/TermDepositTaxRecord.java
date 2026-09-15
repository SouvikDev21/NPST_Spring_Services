package com.term_deposit.backend.entity;

import com.term_deposit.backend.enums.TaxRecordStatus;
import com.term_deposit.backend.enums.TdsStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigInteger;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "term_deposit_tax_record")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TermDepositTaxRecord {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private UUID depositId;
    private String cif;
    private String financialYear;
    private BigInteger interestEarnedMinorUnits;
    private boolean tdsApplicable;
    private BigInteger tdsAmountMinorUnits;
    @Enumerated(EnumType.STRING)
    private TdsStatus tdsStatus;
    @Enumerated(EnumType.STRING)
    private TaxRecordStatus status;
    private String taxSourceReference;
    private String bankCode;
    @Version
    private Long version;
    @CreationTimestamp
    private Instant createdAt;
    @UpdateTimestamp
    private Instant updatedAt;
}