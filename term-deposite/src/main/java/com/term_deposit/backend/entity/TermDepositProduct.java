package com.term_deposit.backend.entity;

import com.term_deposit.backend.enums.DepositType;
import com.term_deposit.backend.enums.ProductRecordStatus;
import com.term_deposit.backend.enums.ProductWorkingStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigInteger;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "term_deposit_product")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TermDepositProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String productCode;

    private String productName;

    @Enumerated(EnumType.STRING)
    private DepositType depositType;

    private String schemeCode;

    private String schemeName;

    @Column(name = "min_amount_minor_units")
    private BigInteger minAmountMinorUnits;

    @Column(name = "max_amount_minor_units")
    private BigInteger maxAmountMinorUnits;

    // --- NEW TENURE FIELDS ADDED TO MATCH DATABASE ---
    @Column(name = "min_tenure_months", nullable = false)
    private Integer minTenureMonths;

    @Column(name = "max_tenure_months", nullable = false)
    private Integer maxTenureMonths;
    // -------------------------------------------------

    @Enumerated(EnumType.STRING)
    private ProductWorkingStatus workingStatus;

    @Enumerated(EnumType.STRING)
    private ProductRecordStatus recordStatus;

    private String bankCode;

    @Version
    private Long version;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}