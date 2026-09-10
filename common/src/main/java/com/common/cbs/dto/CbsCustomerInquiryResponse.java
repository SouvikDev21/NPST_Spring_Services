package com.common.cbs.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CbsCustomerInquiryResponse {

    @JsonProperty("Customer")
    private CbsCustomerProfile customer;

    @JsonProperty("Accounts")
    private Map<String, List<CbsAccountItem>> accounts;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CbsCustomerProfile {
        @JsonProperty("CustomerId")
        private String customerId;

        @JsonProperty("CustomerName")
        private String customerName;

        @JsonProperty("CustomerType")
        private String customerType;

        @JsonProperty("MobileNumber")
        private String mobileNumber;

        @JsonProperty("EmailId")
        private String emailId;

        @JsonProperty("KycStatus")
        private String kycStatus;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CbsAccountItem {
        @JsonProperty("AccountNumber")
        private String accountNumber;

        @JsonProperty("AccountType")
        private String accountType;

        @JsonProperty("ProductCode")
        private String productCode;

        @JsonProperty("ProductName")
        private String productName;

        @JsonProperty("Currency")
        private String currency;

        @JsonProperty("BranchCode")
        private String branchCode;

        @JsonProperty("BranchName")
        private String branchName;

        @JsonProperty("IFSC")
        private String ifsc;

        @JsonProperty("MICR")
        private String micr;

        @JsonProperty("LedgerBalance")
        private BigDecimal ledgerBalance;

        @JsonProperty("AvailableBalance")
        private BigDecimal availableBalance;

        @JsonProperty("LienAmount")
        private BigDecimal lienAmount;

        @JsonProperty("UnclearedBalance")
        private BigDecimal unclearedBalance;

        @JsonProperty("InterestRate")
        private BigDecimal interestRate;

        @JsonProperty("Status")
        private String status;

        @JsonProperty("OpenDate")
        private String openDate;

        @JsonProperty("NomineeRegistered")
        private Boolean nomineeRegistered;

        @JsonProperty("ChequeBookFacility")
        private Boolean chequeBookFacility;

        @JsonProperty("DebitCardActive")
        private Boolean debitCardActive;

        @JsonProperty("JointHolders")
        private Map<String, List<CbsJointHolder>> jointHolders;

        @JsonProperty("Nominees")
        private Map<String, List<CbsNominee>> nominees;

        @JsonProperty("Cards")
        private Map<String, List<CbsCard>> cards;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CbsJointHolder {
        @JsonProperty("CustomerId")
        private String customerId;

        @JsonProperty("Name")
        private String name;

        @JsonProperty("Relationship")
        private String relationship;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CbsNominee {
        @JsonProperty("Name")
        private String name;

        @JsonProperty("Relation")
        private String relation;

        @JsonProperty("SharePercentage")
        private Integer sharePercentage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CbsCard {
        @JsonProperty("CardNumber")
        private String cardNumber;

        @JsonProperty("CardType")
        private String cardType;

        @JsonProperty("Status")
        private String status;

        @JsonProperty("ExpiryDate")
        private String expiryDate;
    }
}
