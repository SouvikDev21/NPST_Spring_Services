package com.fund_transfer.backend.dto.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Request body for POST /api/v1/beneficiaries/otp/send.
 * cif is NOT here — it comes from the X-CIF header like every other
 * beneficiary-management call, so the same header always drives which
 * customer an OTP is issued for.
 */
public record SendBeneficiaryOtpRequest(

        @NotBlank
        @Pattern(regexp = "^[0-9]{10}$", message = "mobileNumber must be a 10-digit number")
        String mobileNumber

) {
}
