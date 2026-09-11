package com.fund_transfer.backend.dto.Response;

import com.fund_transfer.backend.enums.AccountStatus;
import com.fund_transfer.backend.enums.AccountType;
import com.fund_transfer.backend.enums.Currency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponse {

    private UUID id;

    private String accountNumber;

    private String cifId;

    private AccountType accountType;

    private AccountStatus status;

    private BigDecimal balance;

    private BigDecimal availableBalance;

    private Currency currency;

    private String branchCode;

    private String ifscCode;

    private LocalDate openedDate;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}