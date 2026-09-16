package com.fund_transfer.backend.dto.Request;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Outbound request body to the external OTP service's
 * POST /api/v1/app/auth/otp/verify endpoint.
 */
public record OtpVerifyRequest(

        @JsonProperty("otp_reference")
        String otpReference,

        @JsonProperty("otp_code")
        String otpCode,

        @JsonProperty("cif")
        String cif
) {
}
