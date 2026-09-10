package com.fund_transfer.backend.entity;


import java.math.BigInteger;
import java.time.Instant;

import com.fund_transfer.backend.enums.BeneficiaryStatus;
import com.fund_transfer.backend.enums.BeneficiaryType;
import com.fund_transfer.backend.enums.TransferMode;
import jakarta.persistence.*;
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
        name = "beneficiary",
        indexes = {
                @Index(name = "idx_beneficiary_owner_cif", columnList = "owner_cif"),
                @Index(name = "idx_beneficiary_status", columnList = "status")
        }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Beneficiary {

    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "owner_cif", nullable = false, length = 20)
    private String ownerCif; // required — beneficiaries only exist for an actual account holder

    @Column(name = "owner_keycloak_user_id", nullable = false)
    private String ownerKeycloakUserId; // cross-platform audit correlation

    @Column(name = "beneficiary_name", nullable = false, length = 100)
    private String beneficiaryName;

    @Column(name = "beneficiary_account_number", nullable = false, length = 30)
    private String beneficiaryAccountNumber;

    @Column(name = "beneficiary_ifsc_code", nullable = false, length = 11)
    private String beneficiaryIfscCode;

    @Column(name = "beneficiary_bank_name", length = 100)
    private String beneficiaryBankName; // nullable — resolved via IFSC lookup, may not always succeed

    @Column(length = 50)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(name = "transfer_mode", nullable = false, length = 20)
    private TransferMode transferMode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private BeneficiaryStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private BeneficiaryType type;

    @Column(name = "cooling_period_ends_at")
    private Instant coolingPeriodEndsAt; // nullable once status moves past PENDING_COOLING_PERIOD

    @Column(name = "daily_limit_minor_units")
    private BigInteger dailyLimitMinorUnits; // nullable — falls back to the bank/tenant default limit if unset

//    @Column(name = "bank_code", nullable = false, length = 20)
//    private String bankCode; // tenant discriminator — confirm with team whether multi-tenant is in scope

    @Version
    private Long version; // a Checker blocking a beneficiary and the owner using it in-flight is a real race

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    // NOTE: @Setter above generates setBeneficiaryAccountNumber()/setBeneficiaryIfscCode(),
    // which would allow silent mutation of fields that should be immutable post-creation
    // (see BeneficiaryService — service layer never calls these two setters after create()).
    // Consider @Setter(AccessLevel.NONE) on those two fields specifically if Lombok supports
    // per-field override in this codebase's Lombok version.
}
