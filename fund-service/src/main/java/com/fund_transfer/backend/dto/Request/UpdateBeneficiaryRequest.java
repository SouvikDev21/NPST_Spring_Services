package com.fund_transfer.backend.dto.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateBeneficiaryRequest(

        @NotBlank
        @Size(max = 50)
        String nickname
        // beneficiaryAccountNumber / beneficiaryIfscCode intentionally excluded —
        // immutable once created; delete + re-add (with fresh OTP + cooling period) instead.
) {
}