package com.fund_transfer.backend.dto.response.account;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fund_transfer.backend.common.constants.AppConstants;
import com.fund_transfer.backend.enums.AccountStatus;
import com.fund_transfer.backend.enums.AccountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponse {
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

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = AppConstants.ISO_DATETIME_FORMAT, timezone = AppConstants.UTC_ZONE)
    private Instant createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = AppConstants.ISO_DATETIME_FORMAT, timezone = AppConstants.UTC_ZONE)
    private Instant updatedAt;
}
