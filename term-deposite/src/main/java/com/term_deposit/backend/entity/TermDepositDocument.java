package com.term_deposit.backend.entity;

import com.term_deposit.backend.enums.DocumentStatus;
import com.term_deposit.backend.enums.TdDocumentType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "term_deposit_document")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TermDepositDocument {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String documentReference;
    private UUID depositId;
    private String cif;
    @Enumerated(EnumType.STRING)
    private TdDocumentType documentType;
    private String financialYear;
    private String storageReference;
    private String mimeType;
    @Enumerated(EnumType.STRING)
    private DocumentStatus status;
    private Instant generatedAt;
    private String checksum;
    private String bankCode;
    @Version
    private Long version;
    @CreationTimestamp
    private Instant createdAt;
    @UpdateTimestamp
    private Instant updatedAt;
}