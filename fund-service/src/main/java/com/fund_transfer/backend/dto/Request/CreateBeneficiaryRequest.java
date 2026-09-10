package com.fund_transfer.backend.dto.Request;

import com.fund_transfer.backend.enums.BeneficiaryType;
import com.fund_transfer.backend.enums.TransferMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * SECURITY FIX: ownerCif and ownerKeycloakUserId were removed from this DTO.
 * They must NEVER be client-suppliable — a client could otherwise create a
 * beneficiary against another customer's CIF (IDOR). The controller resolves
 * both values from the authenticated security principal instead and passes
 * them into the service layer separately from this request body.
 */
public record CreateBeneficiaryRequest(

        @NotBlank
        @Size(max = 100)
        String beneficiaryName,

        @NotBlank
        @Pattern(regexp = "^[0-9]{9,30}$", message = "beneficiaryAccountNumber must be 9-30 digits")
        String beneficiaryAccountNumber,

        @NotBlank
        @Pattern(
                regexp = "^[A-Z]{4}0[A-Z0-9]{6}$",
                message = "beneficiaryIfscCode must be a valid IFSC code"
        )
        String beneficiaryIfscCode,

        @Size(max = 50)
        String nickname,

        @NotNull
        TransferMode transferMode,

        @NotNull
        BeneficiaryType type

) {
}
