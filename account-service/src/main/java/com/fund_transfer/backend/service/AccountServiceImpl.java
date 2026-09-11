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

        /*
         * Account creation will be completed through the common CBS integration.
         *
         * Expected flow:
         * Request → Common CBS Client → CBS → CBS Account Response
         *         → Map CBS response → Save local Account representation
         *
         * Do not generate account number, IFSC, status, balance,
         * or other CBS-owned values locally.
         */

        throw new UnsupportedOperationException(
                "Account creation requires common CBS integration"
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponse getAccountByAccountNumber(String accountNumber) {

        log.info("Fetching account by account number");

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Account not found for account number: " + accountNumber
                ));

        return accountMapper.toResponse(account);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountResponse> getAccountsByCifId(String cifId) {

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
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Account not found for account number: " + accountNumber
                ));

        return accountMapper.toBalanceResponse(account);
    }

    @Override
    @Transactional
    public AccountResponse updateAccountStatus(
            String accountNumber,
            UpdateAccountStatusRequest request
    ) {

        log.info(
                "Updating status for account to: {}",
                request.getStatus()
        );

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Account not found for account number: " + accountNumber
                ));

        /*
         * Final implementation will call the appropriate CBS operation
         * (freeze/unfreeze) and then update the local representation.
         *
         * Do not change the local status independently of CBS.
         */

        throw new UnsupportedOperationException(
                "Account status update requires common CBS integration"
        );
    }
}