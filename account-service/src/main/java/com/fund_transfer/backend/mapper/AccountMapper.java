package com.fund_transfer.backend.mapper;

import com.fund_transfer.backend.dto.response.account.AccountBalanceResponse;
import com.fund_transfer.backend.dto.response.account.AccountDetailsResponse;
import com.fund_transfer.backend.dto.response.account.AccountResponse;
import com.fund_transfer.backend.entity.Account;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Collections;

@Component
public class AccountMapper {

    public AccountResponse toResponse(Account account) {
        if (account == null) {
            return null;
        }

        return AccountResponse.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .customerId(account.getCustomerId())
                .accountType(account.getAccountType())
                .status(account.getStatus())
                .balance(account.getBalance())
                .availableBalance(account.getAvailableBalance())
                .ledgerBalance(account.getLedgerBalance())
                .currency(account.getCurrency())
                .branchCode(account.getBranchCode())
                .branchName(account.getBranchName())
                .ifscCode(account.getIfscCode())
                .micrCode(account.getMicrCode())
                .interestRate(account.getInterestRate())
                .productCode(account.getProductCode())
                .productName(account.getProductName())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }

    public AccountBalanceResponse toBalanceResponse(Account account) {
        if (account == null) {
            return null;
        }

        return AccountBalanceResponse.builder()
                .accountNumber(account.getAccountNumber())
                .balance(account.getBalance())
                .availableBalance(account.getAvailableBalance())
                .ledgerBalance(account.getLedgerBalance())
                .currency(account.getCurrency())
                .asOf(Instant.now())
                .build();
    }

    public AccountDetailsResponse toDetailsResponse(Account account) {
        if (account == null) {
            return null;
        }

        return AccountDetailsResponse.builder()
                .accountNumber(account.getAccountNumber())
                .customerId(account.getCustomerId())
                .accountType(account.getAccountType())
                .status(account.getStatus())
                .productCode(account.getProductCode())
                .productName(account.getProductName())
                .currency(account.getCurrency())
                .balance(account.getBalance())
                .availableBalance(account.getAvailableBalance())
                .ledgerBalance(account.getLedgerBalance())
                .interestRate(account.getInterestRate())
                .branchCode(account.getBranchCode())
                .branchName(account.getBranchName())
                .ifscCode(account.getIfscCode())
                .micrCode(account.getMicrCode())
                .openDate(account.getCreatedAt() != null ? account.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDate() : null)
                .nomineeRegistered(true)
                .chequeBookFacility(true)
                .debitCardActive(true)
                .cards(Collections.emptyList())
                .jointHolders(Collections.emptyList())
                .nominees(Collections.emptyList())
                .asOf(Instant.now())
                .build();
    }
}
