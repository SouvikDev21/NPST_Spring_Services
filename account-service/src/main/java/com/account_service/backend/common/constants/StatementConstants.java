package com.account_service.backend.common.constants;

public final class StatementConstants {

    private StatementConstants() {
        // Prevent instantiation
    }

    public static final String FORMAT_PDF = "PDF";
    public static final String FORMAT_CSV = "CSV";
    public static final String FORMAT_EXCEL = "EXCEL";

    public static final String CONTENT_TYPE_PDF = "application/pdf";
    public static final String CONTENT_TYPE_CSV = "text/csv";
    public static final String CONTENT_TYPE_EXCEL = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    public static final String STATUS_DISPATCHED = "DISPATCHED";
    public static final String STATUS_GENERATED = "GENERATED";
    public static final String STATUS_FAILED = "FAILED";

    public static final String MSG_STATEMENT_DISPATCHED = "Account statement has been successfully dispatched to ";
    public static final String MSG_STATEMENT_GENERATED = "Account statement has been successfully generated";

    public static final String ERR_INVALID_DATE_RANGE = "Start date must be before or equal to end date";
    public static final String ERR_STATEMENT_ACCOUNT_REQUIRED = "Account number is required for statement requests";
    public static final String ERR_EMAIL_REQUIRED = "Valid email address is required for statement email dispatch";
}
