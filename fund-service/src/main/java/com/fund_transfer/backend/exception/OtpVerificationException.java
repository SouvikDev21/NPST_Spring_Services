package com.fund_transfer.backend.exception;

/**
 * Thrown when an OTP fails verification — wrong code, expired reference,
 * mismatched CIF, or the OTP service being unreachable. Deliberately does
 * NOT distinguish "wrong code" from "expired" in the exception type: the
 * OTP service's response is the source of truth for that distinction, and
 * this service just relays its message rather than trying to re-derive it.
 */
public class OtpVerificationException extends RuntimeException {
    public OtpVerificationException(String message) {
        super(message);
    }

    public OtpVerificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
