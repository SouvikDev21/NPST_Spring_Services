package com.term_deposit.backend.entity;

import com.term_deposit.backend.enums.OutboxStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_event")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OutboxEvent {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String aggregateType;
    private UUID aggregateId;
    private String eventType;
    private Instant processedAt;
    @Column(columnDefinition = "TEXT")
    private String payload;
    @Enumerated(EnumType.STRING)
    private OutboxStatus status;
    private int retryCount;
    private Instant nextAttemptAt;
    private Instant sentAt;
    @Version
    private Long version;
    @CreationTimestamp
    private Instant createdAt;
}