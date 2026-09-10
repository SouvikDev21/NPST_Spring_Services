package com.account_service.backend.dto.statement;

import com.account_service.backend.dto.account.AccountDto.TransactionResponse;
import com.account_service.backend.enums.StatementFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public final class StatementDto {

    private StatementDto() {
    }

    // --- Requests ---

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchRequest {
        @NotBlank(message = "Account number is required")
        private String accountNumber;
        private LocalDate fromDate;
        private LocalDate toDate;
        private Integer page;
        private Integer size;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DownloadRequest {
        @NotBlank(message = "Account number is required")
        private String accountNumber;
        private LocalDate fromDate;
        private LocalDate toDate;
        private StatementFormat format;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmailRequest {
        @NotBlank(message = "Account number is required")
        private String accountNumber;
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String emailAddress;
        private LocalDate fromDate;
        private LocalDate toDate;
        private StatementFormat format;
    }

    // --- Responses ---

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchResponse {
        private String accountId;
        private LocalDate fromDate;
        private LocalDate toDate;
        private BigDecimal openingBalance;
        private BigDecimal closingBalance;
        private BigDecimal totalDebits;
        private BigDecimal totalCredits;
        private String currency;
        private List<TransactionResponse> transactions;
        private Integer totalResults;
        private Integer offset;
        private Integer limit;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DownloadResponse {
        private String accountNumber;
        private LocalDate fromDate;
        private LocalDate toDate;
        private StatementFormat format;
        private String fileName;
        private String contentType;
        private String fileContentBase64;
        private Long fileSizeBytes;
        private Instant generatedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmailResponse {
        private String accountNumber;
        private String recipientEmail;
        private String referenceNumber;
        private String status;
        private String message;
        private Instant dispatchedAt;
    }
}
