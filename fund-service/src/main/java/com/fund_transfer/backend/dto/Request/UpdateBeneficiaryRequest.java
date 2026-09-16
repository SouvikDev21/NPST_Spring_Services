package com.fund_transfer.backend.dto.Request;

import jakarta.validation.constraints.NotBlank;

public record UpdateBeneficiaryRequest(
        @NotBlank
        String nickname
) {
}
