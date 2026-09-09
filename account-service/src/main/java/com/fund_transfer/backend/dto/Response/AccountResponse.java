package com.fund_transfer.backend.dto.Response;

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
    private String currency;
    private String branchCode;
    private String ifscCode;
    private Instant createdAt;
    private Instant updatedAt;
}
