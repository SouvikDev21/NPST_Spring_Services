package com.term_deposit.backend.entity;

import com.term_deposit.backend.enums.DepositRequestStatus;
import com.term_deposit.backend.enums.DepositRequestType;
import com.term_deposit.backend.enums.RequestChannel;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "deposit_request")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DepositRequest {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String requestReference;
    @Enumerated(EnumType.STRING)
    private DepositRequestType requestType;
    private String cif;
    private UUID keycloakUserId;
    @Enumerated(EnumType.STRING)
    private RequestChannel channel;
    private UUID depositId;
    @Enumerated(EnumType.STRING)
    private DepositRequestStatus status;
    private UUID makerKeycloakUserId;
    private UUID checkerKeycloakUserId;
    private Instant reviewedAt;
    private String remarks;
    private LocalDate scheduledExecutionDate;
    private String bankCode;
    @Version
    private Long version;
    @CreationTimestamp
    private Instant createdAt;
    @UpdateTimestamp
    private Instant updatedAt;
}