package com.fund_transfer.backend.service;

import com.fund_transfer.backend.dto.Request.CreateAccountRequest;
import com.fund_transfer.backend.dto.Request.UpdateAccountStatusRequest;
import com.fund_transfer.backend.dto.Response.AccountBalanceResponse;
import com.fund_transfer.backend.dto.Response.AccountResponse;

import java.util.List;

public interface AccountService {

    AccountResponse createAccount(CreateAccountRequest request);

    AccountResponse getAccountByAccountNumber(String accountNumber);

    List<AccountResponse> getAccountsByCustomerId(String customerId);

    AccountBalanceResponse getAccountBalance(String accountNumber);

    AccountResponse updateAccountStatus(String accountNumber, UpdateAccountStatusRequest request);
}
