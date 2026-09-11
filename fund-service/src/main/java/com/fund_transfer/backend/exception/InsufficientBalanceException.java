package com.fund_transfer.backend.exception;

// Thrown in Step 1 (validation) — no money has moved yet, safe to just reject.
public class InsufficientBalanceException extends RuntimeException {
    public InsufficientBalanceException(String message) {
        super(message);
    }
}
