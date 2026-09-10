package com.account_service.backend.common.exception;

public class LimitException extends RuntimeException {
    public LimitException(String message) {
        super(message);
    }
}
