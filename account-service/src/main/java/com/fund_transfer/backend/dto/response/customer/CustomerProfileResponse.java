package com.fund_transfer.backend.dto.response.customer;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fund_transfer.backend.common.constants.AppConstants;
import com.fund_transfer.backend.enums.KycStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerProfileResponse {
    private String customerId;
    private String customerName;
    private String customerType;
    private String mobileNumber;
    private String emailId;
    private KycStatus kycStatus;

    @JsonFormat(pattern = AppConstants.ISO_DATE_FORMAT)
    private LocalDate kycVerifiedDate;

    private String panNumber;
    private String aadhaarMasked;
    private String address;

    @JsonFormat(pattern = AppConstants.ISO_DATE_FORMAT)
    private LocalDate dateOfBirth;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = AppConstants.ISO_DATETIME_FORMAT, timezone = AppConstants.UTC_ZONE)
    private Instant createdAt;
}
