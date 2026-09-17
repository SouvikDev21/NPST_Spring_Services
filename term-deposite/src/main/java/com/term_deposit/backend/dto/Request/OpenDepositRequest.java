package com.term_deposit.backend.dto.Request;

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

        // --- Hardcoded validation removed. Now handled dynamically by the Business Rule Engine! ---
        @NotNull(message = "Principal amount is required")
        BigDecimal principalAmount,

        @NotNull(message = "Tenure in months is required")
        Integer tenureMonths,

        @NotNull(message = "Auto-renewal preference is required")
        Boolean autoRenewal
) {}