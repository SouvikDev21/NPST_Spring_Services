package com.fund_transfer.backend.exception;

/** Thrown when the OTP service could not be reached or refused to send an OTP. */
public class OtpSendException extends RuntimeException {
    public OtpSendException(String message) {
        super(message);
    }

    public OtpSendException(String message, Throwable cause) {
        super(message, cause);
    }
}
