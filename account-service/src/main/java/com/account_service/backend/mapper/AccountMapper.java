package com.account_service.backend.mapper;

import com.account_service.backend.dto.account.AccountDto;
import com.account_service.backend.entity.Account;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Collections;

@Component
public class AccountMapper {

    public AccountDto.AccountResponse toResponse(Account account) {
        if (account == null) {
            return null;
        }

        return AccountDto.AccountResponse.builder()
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

    public AccountDto.BalanceResponse toBalanceResponse(Account account) {
        if (account == null) {
            return null;
        }

        return AccountDto.BalanceResponse.builder()
                .accountNumber(account.getAccountNumber())
                .balance(account.getBalance())
                .availableBalance(account.getAvailableBalance())
                .ledgerBalance(account.getLedgerBalance())
                .currency(account.getCurrency())
                .asOf(Instant.now())
                .build();
    }

    public AccountDto.DetailsResponse toDetailsResponse(Account account) {
        if (account == null) {
            return null;
        }

        return AccountDto.DetailsResponse.builder()
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
