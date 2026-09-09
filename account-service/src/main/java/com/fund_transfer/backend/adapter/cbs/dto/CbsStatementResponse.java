package com.fund_transfer.backend.adapter.cbs.dto;

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
public class CbsStatementResponse {

    @JsonProperty("AccountId")
    private String accountId;

    @JsonProperty("FromDate")
    private String fromDate;

    @JsonProperty("ToDate")
    private String toDate;

    @JsonProperty("OpeningBalance")
    private BigDecimal openingBalance;

    @JsonProperty("ClosingBalance")
    private BigDecimal closingBalance;

    @JsonProperty("TotalDebits")
    private BigDecimal totalDebits;

    @JsonProperty("TotalCredits")
    private BigDecimal totalCredits;

    @JsonProperty("Transactions")
    private Map<String, List<CbsTransactionRecord>> transactions;

    @JsonProperty("Paging")
    private CbsPaging paging;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CbsTransactionRecord {
        @JsonProperty("TransactionId")
        private String transactionId;

        @JsonProperty("TransactionDate")
        private String transactionDate;

        @JsonProperty("ValueDate")
        private String valueDate;

        @JsonProperty("Type")
        private String type;

        @JsonProperty("Amount")
        private BigDecimal amount;

        @JsonProperty("Currency")
        private String currency;

        @JsonProperty("BalanceAfter")
        private BigDecimal balanceAfter;

        @JsonProperty("Narration")
        private String narration;

        @JsonProperty("ReferenceNo")
        private String referenceNo;

        @JsonProperty("Channel")
        private String channel;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CbsPaging {
        @JsonProperty("totalResults")
        private Integer totalResults;

        @JsonProperty("offset")
        private Integer offset;

        @JsonProperty("limit")
        private Integer limit;
    }
}
