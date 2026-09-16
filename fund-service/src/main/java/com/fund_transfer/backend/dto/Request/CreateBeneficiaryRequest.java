package com.fund_transfer.backend.dto.Request;

import com.fund_transfer.backend.enums.BeneficiaryType;
import com.fund_transfer.backend.enums.TransferMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * SECURITY FIX (kept from original design): ownerCif and ownerKeycloakUserId
 * are NOT fields on this DTO. The CIF for a create-beneficiary request comes
 * from the "X-CIF" header (see BeneficiaryController) rather than the body,
 * and ownerKeycloakUserId always comes from the validated Keycloak access
 * token's subject claim — never from client input.
 *
 * OTP: otpReference/otpCode are required and are verified against the OTP
 * service (see BeneficiaryService.create) BEFORE the beneficiary is
 * persisted. otpReference must be the value returned by
 * POST /api/v1/beneficiaries/otp/send for the SAME cif.
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
        BeneficiaryType type,

        @NotBlank(message = "otpReference is required — call /api/v1/beneficiaries/otp/send first")
        String otpReference,

        @NotBlank(message = "otpCode is required")
        @Pattern(regexp = "^[0-9]{4,8}$", message = "otpCode must be numeric")
        String otpCode

) {
}