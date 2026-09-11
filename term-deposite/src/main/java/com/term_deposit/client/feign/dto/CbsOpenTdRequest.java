package com.term_deposit.client.feign.dto;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record CbsOpenTdRequest(
        @JsonProperty("CustomerId")
        String cif,
        String productCode,
        BigDecimal principal,
        Integer tenureMonths,
        String fundingAccountNumber,
        String interestPayoutMode
) {}