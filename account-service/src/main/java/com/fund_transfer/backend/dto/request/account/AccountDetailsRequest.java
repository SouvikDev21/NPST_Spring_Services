package com.fund_transfer.backend.dto.request.account;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDetailsRequest {
    @NotBlank(message = "Account number is required")
    private String accountNumber;

    private String customerId;
}
