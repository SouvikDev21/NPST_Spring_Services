package com.fund_transfer.backend.dto.Response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Envelope returned by the external OTP service's /otp/send endpoint.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OtpSendResponse(
        boolean success,

        @JsonProperty("response_code")
        String responseCode,

        String message,

        String timestamp,

        OtpSendData data
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OtpSendData(

            @JsonProperty("otp_reference")
            String otpReference,

            @JsonProperty("masked_mobile")
            String maskedMobile,

            @JsonProperty("expiry_seconds")
            int expirySeconds,

            // Present only in the OTP service's non-prod/demo environment.
            // Pass-through as received here; do NOT rely on this field being
            // present once pointed at a production OTP service, and strip it
            // at the BFF/frontend layer before showing it to real end users.
            @JsonProperty("demo_otp_hint")
            String demoOtpHint
    ) {
    }
}
