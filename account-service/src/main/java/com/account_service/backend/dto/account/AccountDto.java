package com.account_service.backend.dto.account;

import com.account_service.backend.dto.customer.CustomerDto;
import com.account_service.backend.enums.AccountStatus;
import com.account_service.backend.enums.AccountType;
import com.account_service.backend.enums.Currency;
import com.account_service.backend.enums.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class AccountDto {

    private AccountDto() {
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        @NotBlank(message = "Customer ID is required")
        private String customerId;
        @NotNull(message = "Account type is required")
        private AccountType accountType;
        private String currency;
        private BigDecimal initialDeposit;
        private String branchCode;
        private String branchName;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateStatusRequest {
        @NotNull(message = "Account status is required")
        private AccountStatus status;
        private String reason;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchRequest {
        private String customerId;
        private String accountNumber;
        private AccountStatus status;
        private AccountType accountType;
        private Integer page;
        private Integer size;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetailsRequest {
        @NotBlank(message = "Account number is required")
        private String accountNumber;
        private String customerId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BalanceRequest {
        @NotBlank(message = "Account number is required")
        private String accountNumber;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MiniStatementRequest {
        @NotBlank(message = "Account number is required")
        private String accountNumber;
        private Integer count;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransactionsRequest {
        @NotBlank(message = "Account number is required")
        private String accountNumber;
        private LocalDate fromDate;
        private LocalDate toDate;
        private TransactionType transactionType;
        private Integer page;
        private Integer size;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BeneficiariesRequest {
        private String accountNumber;
        private String customerId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LinkedCardsRequest {
        @NotBlank(message = "Account number is required")
        private String accountNumber;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccountResponse {
        private UUID id;
        private String accountNumber;
        private String customerId;
        private AccountType accountType;
        private AccountStatus status;
        private BigDecimal balance;
        private BigDecimal availableBalance;
        private BigDecimal ledgerBalance;
        private String currency;
        private String branchCode;
        private String branchName;
        private String ifscCode;
        private String micrCode;
        private BigDecimal interestRate;
        private String productCode;
        private String productName;
        private Instant createdAt;
        private Instant updatedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BalanceResponse {
        private String accountNumber;
        private BigDecimal balance;
        private BigDecimal availableBalance;
        private BigDecimal ledgerBalance;
        private String currency;
        private Instant asOf;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetailsResponse {
        private String accountNumber;
        private String customerId;
        private String customerName;
        private AccountType accountType;
        private AccountStatus status;
        private String productCode;
        private String productName;
        private String currency;
        private BigDecimal balance;
        private BigDecimal availableBalance;
        private BigDecimal ledgerBalance;
        private BigDecimal lienAmount;
        private BigDecimal unclearedBalance;
        private BigDecimal interestRate;
        private String branchCode;
        private String branchName;
        private String ifscCode;
        private String micrCode;
        private LocalDate openDate;
        private Boolean nomineeRegistered;
        private Boolean chequeBookFacility;
        private Boolean debitCardActive;
        private List<DebitCardResponse> cards;
        private List<CustomerDto.JointHolderDto> jointHolders;
        private List<CustomerDto.NomineeDto> nominees;
        private Instant asOf;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MiniStatementResponse {
        private String accountNumber;
        private BigDecimal availableBalance;
        private BigDecimal ledgerBalance;
        private String currency;
        private List<TransactionResponse> transactions;
        private Instant asOf;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DebitCardResponse {
        private String cardId;
        private String cardNumber;
        private String cardType;
        private String status;
        private String expiryDate;
        private BigDecimal dailyAtmLimit;
        private BigDecimal dailyPosLimit;
        private Boolean internationalUsage;
        private Boolean contactlessEnabled;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BeneficiaryResponse {
        private UUID id;
        private String accountNumber;
        private String customerId;
        private String beneficiaryName;
        private String beneficiaryAccountNumber;
        private String ifscCode;
        private String bankName;
        private BigDecimal transferLimit;
        private String nickname;
        private String status;
        private Instant createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransactionResponse {
        private UUID id;
        private String transactionId;
        private String transactionRef;
        private String accountNumber;
        private LocalDate transactionDate;
        private LocalDate valueDate;
        private TransactionType transactionType;
        private BigDecimal amount;
        private String currency;
        private BigDecimal balanceAfter;
        private String narration;
        private String referenceNo;
        private String description;
        private String channel;
        private String status;
        private Instant createdAt;
    }
}
