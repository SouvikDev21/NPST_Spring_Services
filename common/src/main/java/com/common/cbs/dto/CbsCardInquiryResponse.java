package com.common.cbs.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CbsCardInquiryResponse {
    @JsonProperty("AccountId")
    private String accountId;

    @JsonProperty("Cards")
    @JsonDeserialize(using = FlexibleMapListDeserializer.class)
    private Map<String, List<CbsCustomerInquiryResponse.CbsCard>> cards;

    public List<CbsCustomerInquiryResponse.CbsCard> getCardList() {
        if (cards == null || cards.isEmpty()) return Collections.emptyList();
        List<CbsCustomerInquiryResponse.CbsCard> list = new ArrayList<>();
        cards.values().forEach(list::addAll);
        return list;
    }
}
