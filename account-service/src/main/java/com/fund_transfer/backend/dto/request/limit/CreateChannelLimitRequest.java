package com.fund_transfer.backend.dto.request.limit;

import com.fund_transfer.backend.enums.LimitChannel;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateChannelLimitRequest {

    @NotNull(message = "Channel is required")
    private LimitChannel channel;

    @NotNull(message = "Per transaction limit is required")
    @DecimalMin(value = "0.01", message = "Per transaction limit must be greater than 0")
    private BigDecimal perTransactionLimit;

    @NotNull(message = "Daily limit is required")
    @DecimalMin(value = "0.01", message = "Daily limit must be greater than 0")
    private BigDecimal dailyLimit;

    @NotNull(message = "Monthly limit is required")
    @DecimalMin(value = "0.01", message = "Monthly limit must be greater than 0")
    private BigDecimal monthlyLimit;

    private Integer maxTransactionsPerDay;
    private String currency;
    private String remarks;
}
