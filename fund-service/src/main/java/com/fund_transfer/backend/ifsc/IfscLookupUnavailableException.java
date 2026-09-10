package com.fund_transfer.backend.ifsc;

public class IfscLookupUnavailableException extends RuntimeException {

    public IfscLookupUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
