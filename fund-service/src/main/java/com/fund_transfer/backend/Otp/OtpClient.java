package com.fund_transfer.backend.Otp;

import com.fund_transfer.backend.dto.Response.OtpSendResponse;

import java.math.BigDecimal;

public interface OtpClient {

    /**
     * Requests an OTP be sent to the customer's registered mobile number.
     *
     * @return the OTP service's "data" payload (otp_reference, masked_mobile,
     *         expiry_seconds, demo_otp_hint) — to be handed straight back to
     *         the frontend so it can prompt for and later submit the code.
     * @throws com.fund_transfer.backend.exception.OtpSendException if the OTP
     *         service is unreachable or responds with success=false.
     */
    OtpSendResponse.OtpSendData sendOtp(String cif, String mobileNumber, String purpose, BigDecimal amount);

    /**
     * Verifies a previously-sent OTP.
     *
     * @throws com.fund_transfer.backend.exception.OtpVerificationException if
     *         verification fails for any reason (wrong code, expired
     *         reference, service unreachable, success=false in the response).
     *         Callers should treat any exception from this method as
     *         "OTP not verified" — never proceed with the protected action.
     */
    void verifyOtp(String otpReference, String otpCode, String cif);
}
