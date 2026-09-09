package com.fund_transfer.backend.service;

import com.fund_transfer.backend.common.constants.AccountConstants;
import com.fund_transfer.backend.common.exception.ResourceNotFoundException;
import com.fund_transfer.backend.dto.Mapper.AccountMapper;
import com.fund_transfer.backend.dto.Request.CreateAccountRequest;
import com.fund_transfer.backend.dto.Request.UpdateAccountStatusRequest;
import com.fund_transfer.backend.dto.Response.AccountBalanceResponse;
import com.fund_transfer.backend.dto.Response.AccountResponse;
import com.fund_transfer.backend.entity.Account;
import com.fund_transfer.backend.enums.AccountStatus;
import com.fund_transfer.backend.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;

    @Override
    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request) {
        log.info("Creating account for customer: {}, type: {}", request.getCustomerId(), request.getAccountType());

        String generatedAccountNumber = generateUniqueAccountNumber();
        BigDecimal initialDeposit = request.getInitialDeposit() != null ? request.getInitialDeposit() : BigDecimal.ZERO;
        String currency = request.getCurrency() != null ? request.getCurrency() : AccountConstants.DEFAULT_CURRENCY;
        String branchCode = request.getBranchCode() != null ? request.getBranchCode() : AccountConstants.DEFAULT_BRANCH_CODE;
        String ifscCode = AccountConstants.DEFAULT_IFSC_PREFIX + branchCode;

        Account account = Account.builder()
                .accountNumber(generatedAccountNumber)
                .customerId(request.getCustomerId())
                .accountType(request.getAccountType())
                .status(AccountStatus.ACTIVE)
                .balance(initialDeposit)
                .availableBalance(initialDeposit)
                .currency(currency)
                .branchCode(branchCode)
                .ifscCode(ifscCode)
                .build();

        Account saved = accountRepository.save(account);
        log.info("Account created successfully with account number: {}", saved.getAccountNumber());
        return accountMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponse getAccountByAccountNumber(String accountNumber) {
        log.info("Fetching account by account number: {}", accountNumber);
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException(AccountConstants.ERR_ACCOUNT_NOT_FOUND + accountNumber));
        return accountMapper.toResponse(account);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountResponse> getAccountsByCustomerId(String customerId) {
        log.info("Fetching all accounts for customer: {}", customerId);
        List<Account> accounts = accountRepository.findByCustomerId(customerId);
        return accounts.stream()
                .map(accountMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AccountBalanceResponse getAccountBalance(String accountNumber) {
        log.info("Fetching balance for account: {}", accountNumber);
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException(AccountConstants.ERR_ACCOUNT_NOT_FOUND + accountNumber));
        return accountMapper.toBalanceResponse(account);
    }

    @Override
    @Transactional
    public AccountResponse updateAccountStatus(String accountNumber, UpdateAccountStatusRequest request) {
        log.info("Updating status of account {} to {}", accountNumber, request.getStatus());
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException(AccountConstants.ERR_ACCOUNT_NOT_FOUND + accountNumber));

        account.setStatus(request.getStatus());
        Account updated = accountRepository.save(account);
        log.info("Account {} status updated to {}", accountNumber, updated.getStatus());
        return accountMapper.toResponse(updated);
    }

    private String generateUniqueAccountNumber() {
        Random random = new Random();
        String accountNumber;
        do {
            long number = 100000000000L + (long) (random.nextDouble() * 899999999999L);
            accountNumber = String.valueOf(number);
        } while (accountRepository.existsByAccountNumber(accountNumber));
        return accountNumber;
    }
}
