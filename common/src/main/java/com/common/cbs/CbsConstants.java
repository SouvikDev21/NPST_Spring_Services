package com.common.cbs;

public final class CbsConstants {

    private CbsConstants() {
    }

    public static final String PATH_V3_CUSTOMER_ACCOUNTS_INQUIRY = "/customers/accounts/inquiry";
    public static final String PATH_V3_CUSTOMER_BASIC_INQUIRY = "/customers/accounts/basic-inquiry";
    public static final String PATH_V3_ACCOUNT_DETAILS_INQUIRY = "/accounts/details/inquiry";
    public static final String PATH_V3_ACCOUNT_STATEMENTS = "/accounts/statements";
    public static final String PATH_V3_CARDS_INQUIRY = "/cards/inquiry";
    public static final String PATH_V1_MOBILE_VALIDATE = "/customers/mobile/validate";
    public static final String PATH_V3_ACCOUNT_OPENING_BALANCE = "/accounts/opening-balance";
    public static final String PATH_V3_ACCOUNT_FREEZE = "/accounts/freeze";
    public static final String PATH_V3_ACCOUNT_UNFREEZE = "/accounts/unfreeze";
    public static final String PATH_V3_INTEREST_CERTIFICATE = "/certificates/interest";
    public static final String PATH_V3_TDS_CERTIFICATE = "/certificates/tds";

    public static final String HEADER_CHANNEL_ID = "X-Channel-Id";
    public static final String HEADER_USER_ID = "X-User-Id";
    public static final String HEADER_BRANCH_CODE = "X-Branch-Code";
}
