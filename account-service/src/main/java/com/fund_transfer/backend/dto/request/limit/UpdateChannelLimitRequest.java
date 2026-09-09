package com.fund_transfer.backend.dto.request.limit;

import com.fund_transfer.backend.enums.LimitChannel;
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
public class UpdateChannelLimitRequest {
    private UUID limitId;
    private LimitChannel channel;
    private BigDecimal perTransactionLimit;
    private BigDecimal dailyLimit;
    private BigDecimal monthlyLimit;
    private Integer maxTransactionsPerDay;
    private LimitStatus status;
    private String remarks;
}
