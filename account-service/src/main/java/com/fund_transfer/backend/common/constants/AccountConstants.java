package com.fund_transfer.backend.common.constants;

public final class AccountConstants {

    private AccountConstants() {
        // Prevent instantiation
    }

    // Default Account Configurations
    public static final String DEFAULT_CURRENCY = "INR";
    public static final String DEFAULT_BRANCH_CODE = "001";
    public static final String DEFAULT_IFSC_PREFIX = "BHRT000";

    // Account Number Generation & Validation
    public static final int ACCOUNT_NUMBER_LENGTH = 12;
    public static final String ACCOUNT_NUMBER_REGEX = "^[0-9]{10,16}$";

    // HTTP Headers
    public static final String HEADER_CUSTOMER_ID = "X-Customer-Id";
    public static final String HEADER_IDEMPOTENCY_KEY = "Idempotency-Key";

    // Error Messages
    public static final String ERR_ACCOUNT_NOT_FOUND = "Account not found for account number: ";
    public static final String ERR_CUSTOMER_ACCOUNTS_NOT_FOUND = "No accounts found for customer ID: ";
    public static final String ERR_ACCOUNT_ALREADY_EXISTS = "Account already exists with account number: ";
    public static final String ERR_INVALID_STATUS_TRANSITION = "Invalid account status transition from %s to %s";
    public static final String ERR_INSUFFICIENT_FUNDS = "Insufficient balance in account: ";
}
