package com.fund_transfer.backend.service;

import com.fund_transfer.backend.common.exception.ResourceNotFoundException;
import com.fund_transfer.backend.dto.Mapper.AccountMapper;
import com.fund_transfer.backend.dto.Request.CreateAccountRequest;
import com.fund_transfer.backend.dto.Request.UpdateAccountStatusRequest;
import com.fund_transfer.backend.dto.Response.AccountBalanceResponse;
import com.fund_transfer.backend.dto.Response.AccountResponse;
import com.fund_transfer.backend.entity.Account;
import com.fund_transfer.backend.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;

    @Override
    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request) {

        log.info(
                "Creating local account representation for CIF: {}, type: {}",
                request.getCifId(),
                request.getAccountType()
        );

        Account account = Account.builder()
                .cifId(request.getCifId())
                .accountType(request.getAccountType())
                .balance(request.getInitialDeposit())
                .availableBalance(request.getInitialDeposit())
                .currency(request.getCurrency())
                .branchCode(request.getBranchCode())
                .build();

        Account savedAccount = accountRepository.save(account);

        log.info(
                "Local account representation created with ID: {}",
                savedAccount.getId()
        );

        return accountMapper.toResponse(savedAccount);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponse getAccountByAccountNumber(String accountNumber) {

        log.info("Fetching account by account number");

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Account not found for account number: " + accountNumber
                        )
                );

        return accountMapper.toResponse(account);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountResponse> getAccountsByCustomerId(String cifId) {

        log.info("Fetching accounts for CIF");

        List<Account> accounts = accountRepository.findByCifId(cifId);

        return accounts.stream()
                .map(accountMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AccountBalanceResponse getAccountBalance(String accountNumber) {

        log.info("Fetching balance for account");

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Account not found for account number: " + accountNumber
                        )
                );

        return accountMapper.toBalanceResponse(account);
    }

    @Override
    @Transactional
    public AccountResponse updateAccountStatus(
            String accountNumber,
            UpdateAccountStatusRequest request
    ) {

        log.info(
                "Updating status of account to: {}",
                request.getStatus()
        );

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Account not found for account number: " + accountNumber
                        )
                );

        account.setStatus(request.getStatus());

        return accountMapper.toResponse(account);
    }
}