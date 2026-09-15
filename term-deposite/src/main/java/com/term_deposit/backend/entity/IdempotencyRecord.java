package com.term_deposit.backend.entity;

import com.term_deposit.backend.enums.IdempotencyStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "idempotency_record")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class IdempotencyRecord {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(unique = true, nullable = false)
    private String idempotencyKey;
    private String requestHash;
    @Column(columnDefinition = "TEXT")
    private String responseBody;
    private Integer httpStatus;
    @Enumerated(EnumType.STRING)
    private IdempotencyStatus status;
    private Instant expiresAt;
    @CreationTimestamp
    private Instant createdAt;
}