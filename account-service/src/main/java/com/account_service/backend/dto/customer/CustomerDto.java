package com.account_service.backend.dto.customer;

import com.account_service.backend.dto.account.AccountDto;
import com.account_service.backend.enums.KycStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public final class CustomerDto {

    private CustomerDto() {
    }

    // --- Requests ---

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchRequest {
        private String searchKey;
        private KycStatus kycStatus;
        private String customerType;
        private Integer page;
        private Integer size;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerRequest {
        private String customerId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccountsRequest {
        private String customerId;
        private Integer page;
        private Integer size;
    }

    // --- Responses ---

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProfileResponse {
        private String customerId;
        private String customerName;
        private String customerType;
        private String mobileNumber;
        private String emailId;
        private KycStatus kycStatus;
        private LocalDate kycVerifiedDate;
        private String panNumber;
        private String aadhaarMasked;
        private String address;
        private LocalDate dateOfBirth;
        private Integer totalAccounts;
        private Instant createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SummaryResponse {
        private String customerId;
        private String customerName;
        private String customerType;
        private String mobileNumber;
        private String emailId;
        private KycStatus kycStatus;
        private Integer totalAccounts;
        private BigDecimal totalBalance;
        private String currency;
        private List<AccountDto.AccountResponse> accounts;
        private Instant asOf;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KycResponse {
        private String customerId;
        private KycStatus kycStatus;
        private LocalDate verifiedDate;
        private String panNumber;
        private String aadhaarMasked;
        private String documentType;
        private String verificationMode;
        private String remarks;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RelationshipResponse {
        private String customerId;
        private List<JointHolderDto> jointHolders;
        private List<NomineeDto> nominees;
        private List<String> authorizedSignatories;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JointHolderDto {
        private String customerId;
        private String name;
        private String relationship;
        private String cif;
        private BigDecimal percentage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NomineeDto {
        private String name;
        private String relation;
        private String relationship;
        private LocalDate dateOfBirth;
        private Integer sharePercentage;
        private BigDecimal allocationPercentage;
        private String guardianName;
        private Boolean minor;
    }
}
