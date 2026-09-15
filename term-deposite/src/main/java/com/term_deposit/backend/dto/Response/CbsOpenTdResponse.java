package com.term_deposit.backend.dto.Response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CbsOpenTdResponse(
        String cbsReferenceNumber,
        String status,
        BigDecimal appliedInterestRate,
        LocalDate maturityDate,
        BigDecimal maturityAmount
) {}