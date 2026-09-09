package com.fund_transfer.backend.adapter.cbs.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CbsCardInquiryResponse {

    @JsonProperty("AccountId")
    private String accountId;

    @JsonProperty("Cards")
    private Map<String, List<CbsCustomerInquiryResponse.CbsCard>> cards;
}
