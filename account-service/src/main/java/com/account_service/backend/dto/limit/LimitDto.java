package com.account_service.backend.dto.limit;

import com.account_service.backend.enums.LimitAction;
import com.account_service.backend.enums.LimitChannel;
import com.account_service.backend.enums.LimitStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public final class LimitDto {

    private LimitDto() {
    }

    // ==========================================
    // Channel Limit DTOs
    // ==========================================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChannelSearchRequest {
        private LimitChannel channel;
        private Integer page;
        private Integer size;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChannelDetailsRequest {
        private UUID id;
        private LimitChannel channel;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChannelHistoryRequest {
        private LimitChannel channel;
        private Integer page;
        private Integer size;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateChannelRequest {
        @NotNull(message = "Channel is required")
        private LimitChannel channel;
        private BigDecimal dailyLimit;
        private BigDecimal monthlyLimit;
        private BigDecimal perTransactionLimit;
        private Integer maxTransactionsPerDay;
        private String currency;
        private Instant effectiveFrom;
        private Instant effectiveTo;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateChannelRequest {
        private UUID id;
        private LimitChannel channel;
        private BigDecimal dailyLimit;
        private BigDecimal monthlyLimit;
        private BigDecimal perTransactionLimit;
        private Integer maxTransactionsPerDay;
        private LimitStatus status;
        private Instant effectiveFrom;
        private Instant effectiveTo;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChannelResponse {
        private UUID id;
        private LimitChannel channel;
        private BigDecimal dailyLimit;
        private BigDecimal monthlyLimit;
        private BigDecimal perTransactionLimit;
        private Integer maxTransactionsPerDay;
        private String currency;
        private LimitStatus status;
        private Instant effectiveFrom;
        private Instant effectiveTo;
        private Instant createdAt;
        private Instant updatedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChannelHistoryResponse {
        private UUID id;
        private LimitChannel channel;
        private LimitAction action;
        private BigDecimal dailyLimit;
        private BigDecimal monthlyLimit;
        private BigDecimal perTransactionLimit;
        private Integer maxTransactionsPerDay;
        private String performedBy;
        private String remarks;
        private Instant timestamp;
    }

    // ==========================================
    // Customer Limit DTOs
    // ==========================================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerSearchRequest {
        private String customerId;
        private LimitChannel channel;
        private Integer page;
        private Integer size;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerDetailsRequest {
        private UUID id;
        private String customerId;
        private LimitChannel channel;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerHistoryRequest {
        private String customerId;
        private Integer page;
        private Integer size;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateCustomerRequest {
        @NotBlank(message = "Customer ID is required")
        private String customerId;
        @NotNull(message = "Channel is required")
        private LimitChannel channel;
        private BigDecimal dailyLimit;
        private BigDecimal monthlyLimit;
        private BigDecimal perTransactionLimit;
        private Integer maxTransactionsPerDay;
        private String currency;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateCustomerRequest {
        private UUID id;
        private String customerId;
        private LimitChannel channel;
        private BigDecimal dailyLimit;
        private BigDecimal monthlyLimit;
        private BigDecimal perTransactionLimit;
        private Integer maxTransactionsPerDay;
        private LimitStatus status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerResponse {
        private UUID id;
        private String customerId;
        private LimitChannel channel;
        private BigDecimal dailyLimit;
        private BigDecimal monthlyLimit;
        private BigDecimal perTransactionLimit;
        private Integer maxTransactionsPerDay;
        private String currency;
        private LimitStatus status;
        private Instant createdAt;
        private Instant updatedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerHistoryResponse {
        private UUID id;
        private String customerId;
        private LimitChannel channel;
        private LimitAction action;
        private BigDecimal dailyLimit;
        private BigDecimal monthlyLimit;
        private BigDecimal perTransactionLimit;
        private Integer maxTransactionsPerDay;
        private String performedBy;
        private String remarks;
        private Instant timestamp;
    }

    // ==========================================
    // Global Limit DTOs
    // ==========================================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GlobalSearchRequest {
        private String limitCode;
        private LimitStatus status;
        private Integer page;
        private Integer size;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GlobalDetailsRequest {
        private UUID id;
        private String limitCode;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GlobalHistoryRequest {
        private String limitCode;
        private Integer page;
        private Integer size;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateGlobalRequest {
        @NotBlank(message = "Limit code is required")
        private String limitCode;
        private String limitName;
        private BigDecimal dailyLimit;
        private BigDecimal monthlyLimit;
        private BigDecimal perTransactionLimit;
        private Integer coolingPeriodHours;
        private String description;
        private String currency;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateGlobalRequest {
        private UUID id;
        private String limitCode;
        private String limitName;
        private BigDecimal dailyLimit;
        private BigDecimal monthlyLimit;
        private BigDecimal perTransactionLimit;
        private Integer coolingPeriodHours;
        private String description;
        private LimitStatus status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GlobalResponse {
        private UUID id;
        private String limitCode;
        private String limitName;
        private BigDecimal dailyLimit;
        private BigDecimal monthlyLimit;
        private BigDecimal perTransactionLimit;
        private Integer coolingPeriodHours;
        private String currency;
        private String description;
        private LimitStatus status;
        private Instant createdAt;
        private Instant updatedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GlobalHistoryResponse {
        private UUID id;
        private String limitCode;
        private String limitName;
        private LimitAction action;
        private BigDecimal dailyLimit;
        private BigDecimal monthlyLimit;
        private BigDecimal perTransactionLimit;
        private Integer coolingPeriodHours;
        private String performedBy;
        private String remarks;
        private Instant timestamp;
    }
}
