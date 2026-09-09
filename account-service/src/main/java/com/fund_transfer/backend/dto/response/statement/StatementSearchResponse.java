package com.fund_transfer.backend.dto.response.statement;

import com.fund_transfer.backend.dto.response.account.TransactionResponse;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fund_transfer.backend.common.constants.AppConstants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatementSearchResponse {
    private String accountId;

    @JsonFormat(pattern = AppConstants.ISO_DATE_FORMAT)
    private LocalDate fromDate;

    @JsonFormat(pattern = AppConstants.ISO_DATE_FORMAT)
    private LocalDate toDate;

    private BigDecimal openingBalance;
    private BigDecimal closingBalance;
    private BigDecimal totalDebits;
    private BigDecimal totalCredits;
    private String currency;
    private List<TransactionResponse> transactions;
    private int totalResults;
    private int offset;
    private int limit;
}
