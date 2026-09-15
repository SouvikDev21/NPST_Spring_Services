package com.term_deposit.backend.dto.Request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record OpenDepositRequest(
        @NotBlank(message = "CIF number is required")
        String cif,

        @NotNull(message = "Maker User ID is required")
        UUID makerUserId,

        @NotBlank(message = "Funding account number is required")
        String fundingAccountNumber,

        @NotNull(message = "Principal amount is required")
        @Min(value = 1000, message = "Minimum deposit amount is ₹1,000")
        BigDecimal principalAmount,

        @NotNull(message = "Tenure in months is required")
        @Min(value = 1, message = "Minimum tenure is 1 month")
        Integer tenureMonths,

        @NotNull(message = "Auto-renewal preference is required")
        Boolean autoRenewal
) {}
