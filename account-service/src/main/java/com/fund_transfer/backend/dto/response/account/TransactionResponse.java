package com.fund_transfer.backend.dto.response.account;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fund_transfer.backend.common.constants.AppConstants;
import com.fund_transfer.backend.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private String transactionId;
    private String accountNumber;

    @JsonFormat(pattern = AppConstants.ISO_DATE_FORMAT)
    private LocalDate transactionDate;

    @JsonFormat(pattern = AppConstants.ISO_DATE_FORMAT)
    private LocalDate valueDate;

    private TransactionType transactionType;
    private BigDecimal amount;
    private String currency;
    private BigDecimal balanceAfter;
    private String narration;
    private String referenceNo;
    private String channel;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = AppConstants.ISO_DATETIME_FORMAT, timezone = AppConstants.UTC_ZONE)
    private Instant createdAt;
}
