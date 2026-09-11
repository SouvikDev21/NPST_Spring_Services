package com.common.cbs.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
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
    @JsonDeserialize(using = FlexibleMapListDeserializer.class)
    private Map<String, List<CbsTransactionRecord>> transactions;

    @JsonProperty("Paging")
    private CbsPaging paging;

    public List<CbsTransactionRecord> getTransactionList() {
        if (transactions == null || transactions.isEmpty()) return Collections.emptyList();
        List<CbsTransactionRecord> list = new ArrayList<>();
        transactions.values().forEach(list::addAll);
        return list;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
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
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CbsPaging {
        @JsonProperty("totalResults")
        @JsonAlias({"TotalResults", "total_results", "total"})
        private Integer totalResults;

        @JsonProperty("offset")
        @JsonAlias({"Offset"})
        private Integer offset;

        @JsonProperty("limit")
        @JsonAlias({"Limit"})
        private Integer limit;
    }
}
