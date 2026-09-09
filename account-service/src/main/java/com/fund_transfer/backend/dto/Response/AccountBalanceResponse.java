package com.fund_transfer.backend.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountBalanceResponse {
    private String accountNumber;
    private BigDecimal balance;
    private BigDecimal availableBalance;
    private String currency;
    private Instant asOf;
}
