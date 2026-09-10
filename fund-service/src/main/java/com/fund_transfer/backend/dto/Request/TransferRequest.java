package com.fund_transfer.backend.dto.Request;

import com.fund_transfer.backend.enums.TransferMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.UUID;

public record TransferRequest(

        String idempotencyKey,
        @NotBlank
        String InitiatorAccountNumber,

        @NotBlank
        String initiatorCif,

        @NotNull
        UUID initiatorKeycloakUserId,
        @NotBlank
        String beneficiaryIfsc,

        UUID beneficiaryId,

        @NotBlank
        String destinationAccountNumber,

        @NotBlank
        String destinationIfscCode,

        @NotNull
        @Positive
        BigDecimal amountMinorUnits,

        @NotBlank
        String currency,

        @NotNull
        TransferMode transferMode,

        @NotBlank
        String bankCode,

        String remarks
) {}
