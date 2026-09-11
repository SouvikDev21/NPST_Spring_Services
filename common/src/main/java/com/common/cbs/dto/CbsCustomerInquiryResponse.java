package com.common.cbs.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CbsCustomerInquiryResponse {

    @JsonProperty("Customer")
    private CbsCustomerProfile customer;

    @JsonProperty("Accounts")
    @JsonDeserialize(using = FlexibleMapListDeserializer.class)
    private Map<String, List<CbsAccountItem>> accounts;

    public List<CbsAccountItem> getAccountList() {
        if (accounts == null || accounts.isEmpty()) {
            return Collections.emptyList();
        }
        List<CbsAccountItem> list = new ArrayList<>();
        accounts.values().forEach(list::addAll);
        return list;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
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
    @JsonIgnoreProperties(ignoreUnknown = true)
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
        @JsonDeserialize(using = FlexibleMapListDeserializer.class)
        private Map<String, List<CbsJointHolder>> jointHolders;

        @JsonProperty("Nominees")
        @JsonDeserialize(using = FlexibleMapListDeserializer.class)
        private Map<String, List<CbsNominee>> nominees;

        @JsonProperty("Cards")
        @JsonDeserialize(using = FlexibleMapListDeserializer.class)
        private Map<String, List<CbsCard>> cards;

        public List<CbsJointHolder> getJointHolderList() {
            if (jointHolders == null || jointHolders.isEmpty()) return Collections.emptyList();
            List<CbsJointHolder> list = new ArrayList<>();
            jointHolders.values().forEach(list::addAll);
            return list;
        }

        public List<CbsNominee> getNomineeList() {
            if (nominees == null || nominees.isEmpty()) return Collections.emptyList();
            List<CbsNominee> list = new ArrayList<>();
            nominees.values().forEach(list::addAll);
            return list;
        }

        public List<CbsCard> getCardList() {
            if (cards == null || cards.isEmpty()) return Collections.emptyList();
            List<CbsCard> list = new ArrayList<>();
            cards.values().forEach(list::addAll);
            return list;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
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
    @JsonIgnoreProperties(ignoreUnknown = true)
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
    @JsonIgnoreProperties(ignoreUnknown = true)
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
