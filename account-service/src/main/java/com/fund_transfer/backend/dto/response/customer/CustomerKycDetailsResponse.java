package com.fund_transfer.backend.dto.response.customer;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fund_transfer.backend.common.constants.AppConstants;
import com.fund_transfer.backend.enums.KycStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerKycDetailsResponse {
    private String customerId;
    private KycStatus kycStatus;

    @JsonFormat(pattern = AppConstants.ISO_DATE_FORMAT)
    private LocalDate verifiedDate;

    private String panNumber;
    private String aadhaarMasked;
    private String documentType;
    private String verificationMode;
    private String remarks;
}
