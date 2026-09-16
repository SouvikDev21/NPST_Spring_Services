package com.fund_transfer.backend.Otp;

import com.fund_transfer.backend.dto.Request.OtpSendRequest;
import com.fund_transfer.backend.dto.Request.OtpVerifyRequest;
import com.fund_transfer.backend.dto.Response.OtpSendResponse;
import com.fund_transfer.backend.dto.Response.OtpVerifyResponse;
import com.fund_transfer.backend.exception.OtpSendException;
import com.fund_transfer.backend.exception.OtpVerificationException;
import feign.FeignException;
import feign.RetryableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * OpenFeign-backed replacement for the old OtpRestClient. Same contract
 * (OtpClient) and same exception-mapping behavior, just delegating the HTTP
 * call to OtpFeignClient instead of a hand-built RestClient.
 *
 * - FeignException (any non-2xx) === the old RestClientResponseException
 *   branch: the OTP service answered, just with an error status.
 * - RetryableException (Feign's umbrella for connect/read timeouts and
 *   connection failures) === the old ResourceAccessException branch: we
 *   couldn't reach/finish talking to the OTP service at all.
 *   Note RetryableException extends FeignException, so it's checked first.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "otp.mock-enabled", havingValue = "false", matchIfMissing = true)
public class OtpFeignClientImpl implements OtpClient {

    private final OtpFeignClient otpFeignClient;

    @Override
    public OtpSendResponse.OtpSendData sendOtp(String cif, String mobileNumber, String purpose, BigDecimal amount) {
        OtpSendRequest requestBody = new OtpSendRequest(cif, mobileNumber, purpose, amount);

        try {
            OtpSendResponse response = otpFeignClient.sendOtp(requestBody);

            if (response == null || !response.success() || response.data() == null) {
                String message = response != null ? response.message() : "empty response from OTP service";
                log.warn("OTP send did not succeed for cif={}: {}", cif, message);
                throw new OtpSendException("Failed to send OTP: " + message);
            }

            return response.data();

        } catch (RetryableException e) {
            log.error("OTP send call failed (network/timeout) for cif={}", cif, e);
            throw new OtpSendException("Could not reach OTP service", e);
        } catch (FeignException e) {
            log.error("OTP send rejected, status {}: {}", e.status(), e.contentUTF8());
            throw new OtpSendException("OTP service rejected the send request: " + e.status(), e);
        }
    }

    @Override
    public void verifyOtp(String otpReference, String otpCode, String cif) {
        OtpVerifyRequest requestBody = new OtpVerifyRequest(otpReference, otpCode, cif);

        try {
            OtpVerifyResponse response = otpFeignClient.verifyOtp(requestBody);

            if (response == null || !response.success()) {
                String message = response != null ? response.message() : "empty response from OTP service";
                log.warn("OTP verification failed for cif={}, otpReference={}: {}", cif, otpReference, message);
                throw new OtpVerificationException(message != null ? message : "OTP verification failed");
            }

        } catch (RetryableException e) {
            log.error("OTP verify call failed (network/timeout) for cif={}, otpReference={}", cif, otpReference, e);
            throw new OtpVerificationException("Could not reach OTP service to verify code", e);
        } catch (FeignException e) {
            // The OTP service uses a non-2xx status for wrong/expired codes too, not just
            // outages — surface its message rather than a generic "OTP service failed".
            log.warn("OTP verification rejected, status {}: {}", e.status(), e.contentUTF8());
            throw new OtpVerificationException("OTP verification rejected: " + e.status(), e);
        }
    }
}
