package com.term_deposit.backend.dto.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CloseDepositRequest(
        @NotBlank(message = "Deposit number is required")
        String depositNumber,

        @NotNull(message = "Maker User ID is required")
        UUID makerUserId,

        @NotBlank(message = "CIF is required")
        String cif
) {}