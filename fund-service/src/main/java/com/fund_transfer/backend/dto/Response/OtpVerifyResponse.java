package com.fund_transfer.backend.dto.Response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Envelope returned by the external OTP service's /otp/verify endpoint.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OtpVerifyResponse(
        boolean success,

        @JsonProperty("response_code")
        String responseCode,

        String message,

        String timestamp
) {
}
