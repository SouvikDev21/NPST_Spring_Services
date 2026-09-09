package com.fund_transfer.backend.dto.response.customer;

import com.fund_transfer.backend.dto.response.customer.JointHolderDto;
import com.fund_transfer.backend.dto.response.customer.NomineeDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerRelationshipDetailsResponse {
    private String customerId;
    private List<JointHolderDto> jointHolders;
    private List<NomineeDto> nominees;
    private List<String> authorizedSignatories;
}
