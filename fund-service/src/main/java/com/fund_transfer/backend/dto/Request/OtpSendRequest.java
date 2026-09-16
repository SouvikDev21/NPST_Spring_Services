package com.fund_transfer.backend.dto.Request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * Outbound request body to the external OTP service's
 * POST /api/v1/app/auth/otp/send endpoint.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record OtpSendRequest(

        @JsonProperty("cif")
        String cif,

        @JsonProperty("mobile_number")
        String mobileNumber,

        @JsonProperty("purpose")
        String purpose,

        // Not applicable for ADD_BENEFICIARY — omitted from the JSON body
        // (NON_NULL above) rather than sent as 0/null, since the sample
        // payload only showed "amount" being used for a money-movement purpose.
        @JsonProperty("amount")
        BigDecimal amount
) {
}
