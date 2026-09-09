package com.fund_transfer.backend.dto.response.account;

import com.fund_transfer.backend.dto.response.account.TransactionResponse;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fund_transfer.backend.common.constants.AppConstants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountMiniStatementResponse {
    private String accountNumber;
    private BigDecimal availableBalance;
    private BigDecimal ledgerBalance;
    private String currency;
    private List<TransactionResponse> transactions;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = AppConstants.ISO_DATETIME_FORMAT, timezone = AppConstants.UTC_ZONE)
    private Instant asOf;
}
