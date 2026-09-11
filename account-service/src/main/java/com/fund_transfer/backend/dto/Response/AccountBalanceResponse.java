package com.fund_transfer.backend.dto.Response;

import com.fund_transfer.backend.enums.Currency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountBalanceResponse {

    private String accountNumber;

    private BigDecimal balance;

    private BigDecimal availableBalance;

    private Currency currency;

    private OffsetDateTime asOf;
}