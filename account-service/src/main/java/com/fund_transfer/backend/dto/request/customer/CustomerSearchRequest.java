package com.fund_transfer.backend.dto.request.customer;

import com.fund_transfer.backend.enums.KycStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerSearchRequest {
    private String searchKey;
    private KycStatus kycStatus;
    private String customerType;
    private Integer page;
    private Integer size;
}
