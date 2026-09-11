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
public class CbsAccountDetailsResponse {

    @JsonProperty("Account")
    @JsonDeserialize(using = FlexibleSingleItemDeserializer.class)
    private CbsAccountDetailItem account;

    @JsonProperty("Cards")
    @JsonDeserialize(using = FlexibleMapListDeserializer.class)
    private Map<String, List<CbsCustomerInquiryResponse.CbsCard>> cards;

    @JsonProperty("RelatedParties")
    @JsonDeserialize(using = FlexibleMapListDeserializer.class)
    private Map<String, List<CbsCustomerInquiryResponse.CbsJointHolder>> relatedParties;

    @JsonProperty("Nominees")
    @JsonDeserialize(using = FlexibleMapListDeserializer.class)
    private Map<String, List<CbsCustomerInquiryResponse.CbsNominee>> nominees;

    public List<CbsCustomerInquiryResponse.CbsCard> getCardList() {
        if (cards == null || cards.isEmpty()) return Collections.emptyList();
        List<CbsCustomerInquiryResponse.CbsCard> list = new ArrayList<>();
        cards.values().forEach(list::addAll);
        return list;
    }

    public List<CbsCustomerInquiryResponse.CbsJointHolder> getRelatedPartiesList() {
        if (relatedParties == null || relatedParties.isEmpty()) return Collections.emptyList();
        List<CbsCustomerInquiryResponse.CbsJointHolder> list = new ArrayList<>();
        relatedParties.values().forEach(list::addAll);
        return list;
    }

    public List<CbsCustomerInquiryResponse.CbsNominee> getNomineeList() {
        if (nominees == null || nominees.isEmpty()) return Collections.emptyList();
        List<CbsCustomerInquiryResponse.CbsNominee> list = new ArrayList<>();
        nominees.values().forEach(list::addAll);
        return list;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CbsAccountDetailItem {
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
    }
}
