package com.fund_transfer.backend.common.constants;

public final class CbsConstants {

    private CbsConstants() {
        // Prevent instantiation
    }

    public static final String PATH_V3_CUSTOMER_ACCOUNTS_INQUIRY = "/api/v3/customers/accounts/inquiry";
    public static final String PATH_V3_CUSTOMER_BASIC_INQUIRY = "/api/v3/customers/accounts/basic-inquiry";
    public static final String PATH_V3_ACCOUNT_DETAILS_INQUIRY = "/api/v3/accounts/details/inquiry";
    public static final String PATH_V3_ACCOUNT_STATEMENTS = "/api/v3/accounts/statements";
    public static final String PATH_V3_CARDS_INQUIRY = "/api/v3/cards/inquiry";
    public static final String PATH_V1_MOBILE_VALIDATE = "/api/v1/customers/mobile/validate";
    public static final String PATH_V3_ACCOUNT_OPENING_BALANCE = "/api/v3/accounts/opening-balance";
    public static final String PATH_V3_ACCOUNT_FREEZE = "/api/v3/accounts/freeze";
    public static final String PATH_V3_ACCOUNT_UNFREEZE = "/api/v3/accounts/unfreeze";
    public static final String PATH_V3_INTEREST_CERTIFICATE = "/api/v3/certificates/interest";
    public static final String PATH_V3_TDS_CERTIFICATE = "/api/v3/certificates/tds";

    public static final String HEADER_CHANNEL_ID = "X-Channel-Id";
    public static final String HEADER_USER_ID = "X-User-Id";
    public static final String HEADER_BRANCH_CODE = "X-Branch-Code";
}
