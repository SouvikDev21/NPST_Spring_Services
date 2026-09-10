package com.account_service.backend.common.exception;

public class AccountException extends RuntimeException {

    public AccountException(String message) {
        super(message);
    }

    public AccountException(String message, Throwable cause) {
        super(message, cause);
    }
}
