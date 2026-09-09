package com.fund_transfer.backend.common.constants;

import java.math.BigDecimal;

public final class LimitConstants {

    private LimitConstants() {
        // Prevent instantiation
    }

    // Default Channel Limits
    public static final BigDecimal DEFAULT_CHANNEL_TXN_LIMIT = new BigDecimal("50000.00");
    public static final BigDecimal DEFAULT_CHANNEL_DAILY_LIMIT = new BigDecimal("200000.00");
    public static final BigDecimal DEFAULT_CHANNEL_MONTHLY_LIMIT = new BigDecimal("2000000.00");
    public static final int DEFAULT_CHANNEL_MAX_TXN_PER_DAY = 10;

    // Default Customer Limits
    public static final BigDecimal DEFAULT_CUSTOMER_TXN_LIMIT = new BigDecimal("25000.00");
    public static final BigDecimal DEFAULT_CUSTOMER_DAILY_LIMIT = new BigDecimal("100000.00");
    public static final BigDecimal DEFAULT_CUSTOMER_MONTHLY_LIMIT = new BigDecimal("1000000.00");
    public static final int DEFAULT_CUSTOMER_MAX_TXN_PER_DAY = 5;

    // Default Global Limits
    public static final BigDecimal DEFAULT_GLOBAL_TXN_LIMIT = new BigDecimal("100000.00");
    public static final BigDecimal DEFAULT_GLOBAL_DAILY_LIMIT = new BigDecimal("500000.00");
    public static final BigDecimal DEFAULT_GLOBAL_MONTHLY_LIMIT = new BigDecimal("5000000.00");
    public static final int DEFAULT_COOLING_PERIOD_HOURS = 24;

    // Limit Codes
    public static final String GLOBAL_LIMIT_CODE_DEFAULT = "GLOBAL_DEF_001";
    public static final String GLOBAL_LIMIT_NAME_DEFAULT = "Default System-Wide Transaction Policy";

    // Error Messages
    public static final String ERR_LIMIT_NOT_FOUND = "Limit configuration not found for: ";
    public static final String ERR_LIMIT_ALREADY_EXISTS = "Limit configuration already exists for: ";
    public static final String ERR_CHANNEL_REQUIRED = "Channel type is required";
    public static final String ERR_CUSTOMER_REQUIRED = "Customer ID/CIF is required";
    public static final String ERR_GLOBAL_CODE_REQUIRED = "Global limit code is required";
}
