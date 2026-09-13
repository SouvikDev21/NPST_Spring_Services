package com.fund_transfer.backend.dto.Response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

// Placeholder field names — replace with CBS's real response schema once confirmed.
@JsonIgnoreProperties(ignoreUnknown = true)
public record CbsBalanceResponse(
        String AccountNumber,
        BigDecimal AvailableBalance,
        String Currency
) {
}