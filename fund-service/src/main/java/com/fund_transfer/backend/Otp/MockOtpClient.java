package com.fund_transfer.backend.Otp;

import com.fund_transfer.backend.dto.Response.OtpSendResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * LOCAL/DEV ONLY. When otp.mock-enabled=true (see application.properties),
 * this replaces OtpRestClient entirely — no call to the real OTP service is
 * made at all. sendOtp() fabricates a reference/code locally; verifyOtp()
 * always succeeds, unconditionally, for any reference/code/cif combination.
 *
 * This is a blunt "OTP is always valid" bypass, exactly as asked for — it
 * does NOT check that otpCode matches what sendOtp() handed back, so it
 * won't catch bugs like the frontend forgetting to send otpCode at all
 * (empty string still passes, since @NotBlank on the DTO is the only thing
 * stopping that, not this client). Good enough to unblock testing the rest
 * of the beneficiary flow; not a substitute for testing against the real
 * OTP service before going live.
 *
 * !! MUST be disabled (otp.mock-enabled=false or removed) before any
 * shared/staging/prod deploy !! — leaving this on means beneficiaries can
 * be added with zero actual OTP verification, from anyone who can already
 * pass authentication/authorization.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "otp.mock-enabled", havingValue = "true")
public class MockOtpClient implements OtpClient {

    @Override
    public OtpSendResponse.OtpSendData sendOtp(String cif, String mobileNumber, String purpose, BigDecimal amount) {
        String otpReference = "MOCK-OTP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        log.warn("MOCK OTP SEND (otp.mock-enabled=true) — no real OTP sent. cif={}, mobile={}, purpose={}, " +
                "otpReference={}", cif, mobileNumber, purpose, otpReference);

        return new OtpSendResponse.OtpSendData(
                otpReference,
                maskMobile(mobileNumber),
                180,
                "123456" // any code works in verifyOtp() below anyway
        );
    }

    @Override
    public void verifyOtp(String otpReference, String otpCode, String cif) {
        // Always succeeds — no real check performed.
        log.warn("MOCK OTP VERIFY (otp.mock-enabled=true) — treating as valid without checking anything. " +
                "cif={}, otpReference={}, otpCode={}", cif, otpReference, otpCode);
    }

    private String maskMobile(String mobileNumber) {
        if (mobileNumber == null || mobileNumber.length() < 4) {
            return "XXXXXXXXXX";
        }
        return "XXXXXX" + mobileNumber.substring(mobileNumber.length() - 4);
    }
}