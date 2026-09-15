package com.term_deposit.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "term_deposit_nominee")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TermDepositNominee {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private UUID depositId;
    private String cif;
    private String nomineeName;
    private String relationship;
    private String bankCode;
    @Version
    private Long version;
    @CreationTimestamp
    private Instant createdAt;
    @UpdateTimestamp
    private Instant updatedAt;
}