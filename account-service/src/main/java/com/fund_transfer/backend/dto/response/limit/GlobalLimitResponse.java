package com.fund_transfer.backend.dto.response.limit;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fund_transfer.backend.common.constants.AppConstants;
import com.fund_transfer.backend.enums.LimitStatus;
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
public class GlobalLimitResponse {
    private UUID id;
    private String limitCode;
    private String limitName;
    private BigDecimal perTransactionLimit;
    private BigDecimal dailyLimit;
    private BigDecimal monthlyLimit;
    private Integer coolingPeriodHours;
    private String currency;
    private String description;
    private LimitStatus status;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = AppConstants.ISO_DATETIME_FORMAT, timezone = AppConstants.UTC_ZONE)
    private Instant createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = AppConstants.ISO_DATETIME_FORMAT, timezone = AppConstants.UTC_ZONE)
    private Instant updatedAt;
}
