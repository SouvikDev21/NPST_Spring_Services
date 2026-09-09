package com.fund_transfer.backend.dto.response.account;

import com.fund_transfer.backend.dto.response.account.DebitCardResponse;
import com.fund_transfer.backend.dto.response.customer.JointHolderDto;
import com.fund_transfer.backend.dto.response.customer.NomineeDto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fund_transfer.backend.common.constants.AppConstants;
import com.fund_transfer.backend.enums.AccountStatus;
import com.fund_transfer.backend.enums.AccountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDetailsResponse {
    private String accountNumber;
    private String customerId;
    private String customerName;
    private AccountType accountType;
    private AccountStatus status;
    private String productCode;
    private String productName;
    private String currency;
    private BigDecimal balance;
    private BigDecimal availableBalance;
    private BigDecimal ledgerBalance;
    private BigDecimal lienAmount;
    private BigDecimal unclearedBalance;
    private BigDecimal interestRate;
    private String branchCode;
    private String branchName;
    private String ifscCode;
    private String micrCode;

    @JsonFormat(pattern = AppConstants.ISO_DATE_FORMAT)
    private LocalDate openDate;

    private boolean nomineeRegistered;
    private boolean chequeBookFacility;
    private boolean debitCardActive;

    private List<DebitCardResponse> cards;
    private List<JointHolderDto> jointHolders;
    private List<NomineeDto> nominees;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = AppConstants.ISO_DATETIME_FORMAT, timezone = AppConstants.UTC_ZONE)
    private Instant asOf;
}
