package com.fund_transfer.backend.dto.Request;

import com.fund_transfer.backend.enums.AccountType;
import com.fund_transfer.backend.enums.Currency;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAccountRequest {

    @NotBlank(message = "CIF ID is required")
    private String cifId;

    @NotNull(message = "Account type is required")
    private AccountType accountType;

    @DecimalMin(value = "0.00", message = "Initial deposit cannot be negative")
    private BigDecimal initialDeposit;

    private Currency currency;

    private String branchCode;
}