package com.term_deposit.backend.dto.Request;
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