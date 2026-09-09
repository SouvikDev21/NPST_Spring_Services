package com.fund_transfer.backend.common.constants;

public final class AppConstants {

    private AppConstants() {
        // Prevent instantiation
    }

    // Pagination Constants
    public static final int DEFAULT_PAGE_NUMBER = 0;
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;
    public static final String DEFAULT_SORT_BY = "createdAt";
    public static final String DEFAULT_SORT_DIRECTION = "DESC";

    // Date Format Constants (ISO-8601)
    public static final String ISO_DATE_FORMAT = "yyyy-MM-dd";
    public static final String ISO_DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
    public static final String UTC_ZONE = "UTC";

    // Common Status Values
    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_FAILED = "FAILED";
    public static final String STATUS_PENDING = "PENDING";

    // Currency Constants
    public static final String DEFAULT_CURRENCY = "INR";

    // API Base Paths
    public static final String API_V1_CUSTOMER = "/api/v1/customer";
    public static final String API_V1_CUSTOMERS = "/api/v1/customers";
    public static final String API_V1_ACCOUNTS = "/api/v1/accounts";
    public static final String API_V1_STATEMENTS = "/api/v1/statements";
    public static final String API_V1_LIMITS = "/api/v1/limits";
}
