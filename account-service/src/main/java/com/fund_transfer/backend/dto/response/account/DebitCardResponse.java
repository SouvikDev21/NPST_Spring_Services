package com.fund_transfer.backend.dto.response.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DebitCardResponse {
    private String cardNumber;
    private String cardType;
    private String status;
    private String expiryDate;
    private BigDecimal dailyAtmLimit;
    private BigDecimal dailyPosLimit;
    private boolean internationalUsage;
    private boolean contactlessEnabled;
}
