package com.common.cbs.dto;

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
    @JsonProperty("Cards")
    private Map<String, List<CbsCustomerInquiryResponse.CbsCard>> cards;
}
