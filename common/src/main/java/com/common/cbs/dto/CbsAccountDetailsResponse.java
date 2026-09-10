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
public class CbsAccountDetailsResponse {

    @JsonProperty("Account")
    private CbsAccountDetailItem account;

    @JsonProperty("Cards")
    private Map<String, List<CbsCustomerInquiryResponse.CbsCard>> cards;

    @JsonProperty("RelatedParties")
    private Map<String, List<CbsCustomerInquiryResponse.CbsJointHolder>> relatedParties;

    @JsonProperty("Nominees")
    private Map<String, List<CbsCustomerInquiryResponse.CbsNominee>> nominees;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
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
