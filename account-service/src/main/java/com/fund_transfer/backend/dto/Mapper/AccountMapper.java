package com.fund_transfer.backend.dto.Mapper;

import com.fund_transfer.backend.dto.Response.AccountBalanceResponse;
import com.fund_transfer.backend.dto.Response.AccountResponse;
import com.fund_transfer.backend.entity.Account;
import org.springframework.stereotype.Component;

import java.time.Instant;

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
                .currency(account.getCurrency())
                .branchCode(account.getBranchCode())
                .ifscCode(account.getIfscCode())
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
                .currency(account.getCurrency())
                .asOf(Instant.now())
                .build();
    }
}
