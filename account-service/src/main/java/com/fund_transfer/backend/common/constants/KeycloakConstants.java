package com.fund_transfer.backend.common.constants;

public final class KeycloakConstants {

    private KeycloakConstants() {
        // Prevent instantiation
    }

    public static final String REALM_BHARAT_BANKING = "bharat-banking";

    // Audiences / Keycloak Clients
    public static final String CLIENT_ADMIN_WEB = "admin-web";
    public static final String CLIENT_MOBILE_APP = "mobile-app";

    // Realm Roles from Bharat Banking OpenAPI specification
    public static final String ROLE_BANK_SUPER_ADMIN = "BANK_SUPER_ADMIN";
    public static final String ROLE_BANK_ADMIN = "BANK_ADMIN";
    public static final String ROLE_BANK_MAKER = "BANK_MAKER";
    public static final String ROLE_BANK_CHECKER = "BANK_CHECKER";
    public static final String ROLE_CORPORATE_IT_ADMIN = "CORPORATE_IT_ADMIN";
    public static final String ROLE_CORPORATE_MAKER = "CORPORATE_MAKER";
    public static final String ROLE_CORPORATE_CHECKER = "CORPORATE_CHECKER";
    public static final String ROLE_CORPORATE_VIEWER = "CORPORATE_VIEWER";
    public static final String ROLE_RETAIL_CUSTOMER = "RETAIL_CUSTOMER";

    // Standard & Custom Claims
    public static final String CLAIM_CIF = "cif";
    public static final String CLAIM_CIF_ID = "cif_id";
    public static final String CLAIM_CUSTOMER_ID = "customer_id";
    public static final String CLAIM_PREFERRED_USERNAME = "preferred_username";
    public static final String CLAIM_SUB = "sub";
    public static final String CLAIM_EMAIL = "email";
    public static final String CLAIM_NAME = "name";
    public static final String CLAIM_REALM_ACCESS = "realm_access";
    public static final String CLAIM_ROLES = "roles";

    // HTTP Headers
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";

    public static final String ANONYMOUS_USER = "anonymous";
}
