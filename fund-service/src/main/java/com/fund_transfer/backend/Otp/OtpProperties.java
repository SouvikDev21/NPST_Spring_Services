package com.fund_transfer.backend.Otp;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "otp")
public class OtpProperties {

    // Base URL of the OTP/auth service, e.g. http://103.209.145.243:9101
    private String baseUrl;

    private String sendPath = "/api/v1/app/auth/otp/send";
    private String verifyPath = "/api/v1/app/auth/otp/verify";

    // Purpose string sent on OTP-send for the "add beneficiary" flow.
    // Confirm the exact enum value the OTP service expects — ADD_BENEFICIARY
    // is the natural guess given the sample only showed LOGIN, but this must
    // match the OTP service's actual accepted values.
    private String addBeneficiaryPurpose = "ADD_BENEFICIARY";

    private int connectTimeoutMs = 3000;
    private int readTimeoutMs = 8000;

    // --- Auth to the OTP service itself, if any — fill in once confirmed ---
    private String apiKeyHeaderName = "X-API-Key";
    private String apiKey;
}
