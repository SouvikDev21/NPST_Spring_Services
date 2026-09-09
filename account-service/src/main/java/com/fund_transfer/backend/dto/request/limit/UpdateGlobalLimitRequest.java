package com.fund_transfer.backend.dto.request.limit;

import com.fund_transfer.backend.enums.LimitStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateGlobalLimitRequest {
    private UUID limitId;
    private String limitCode;
    private BigDecimal perTransactionLimit;
    private BigDecimal dailyLimit;
    private BigDecimal monthlyLimit;
    private Integer coolingPeriodHours;
    private LimitStatus status;
    private String description;
}
