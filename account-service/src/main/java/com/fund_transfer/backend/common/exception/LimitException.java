package com.fund_transfer.backend.common.exception;

public class LimitException extends RuntimeException {
    public LimitException(String message) {
        super(message);
    }
}
