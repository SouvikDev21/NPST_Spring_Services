package com.account_service.backend.entity;

import com.account_service.backend.enums.KycStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(
        name = "customer_profiles",
        indexes = {
                @Index(name = "idx_cust_cif", columnList = "customer_id", unique = true),
                @Index(name = "idx_cust_mobile", columnList = "mobile_number"),
                @Index(name = "idx_cust_email", columnList = "email_id")
        }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "customer_id", nullable = false, unique = true, length = 50)
    private String customerId;

    @Column(name = "customer_name", nullable = false, length = 100)
    private String customerName;

    @Column(name = "customer_type", nullable = false, length = 30)
    private String customerType;

    @Column(name = "mobile_number", length = 20)
    private String mobileNumber;

    @Column(name = "email_id", length = 100)
    private String emailId;

    @Enumerated(EnumType.STRING)
    @Column(name = "kyc_status", nullable = false, length = 30)
    private KycStatus kycStatus;

    @Column(name = "kyc_verified_date")
    private LocalDate kycVerifiedDate;

    @Column(name = "pan_number", length = 20)
    private String panNumber;

    @Column(name = "aadhaar_masked", length = 20)
    private String aadhaarMasked;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

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
