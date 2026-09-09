package com.fund_transfer.backend.common.constants;

public final class CustomerConstants {

    private CustomerConstants() {
        // Prevent instantiation
    }

    public static final String DEFAULT_CUSTOMER_TYPE = "INDIVIDUAL";
    public static final String DEFAULT_KYC_STATUS = "COMPLETED";
    public static final String DEFAULT_CUSTOMER_ID = "CIF100001";

    public static final String ERR_CUSTOMER_NOT_FOUND = "Customer not found for CIF: ";
    public static final String ERR_CUSTOMER_ID_REQUIRED = "Customer ID/CIF is required or token must contain valid CIF claim";
    public static final String ERR_KYC_NOT_FOUND = "KYC details not found for customer: ";
    public static final String ERR_RELATIONSHIPS_NOT_FOUND = "Relationship details not found for customer: ";
}
