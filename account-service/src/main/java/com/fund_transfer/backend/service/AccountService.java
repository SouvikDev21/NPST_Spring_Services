package com.fund_transfer.backend.service;

import com.fund_transfer.backend.dto.common.PageResponse;
import com.fund_transfer.backend.dto.request.account.AccountBalanceRequest;
import com.fund_transfer.backend.dto.request.account.AccountBeneficiariesRequest;
import com.fund_transfer.backend.dto.request.account.AccountDetailsRequest;
import com.fund_transfer.backend.dto.request.account.AccountLinkedCardsRequest;
import com.fund_transfer.backend.dto.request.account.AccountMiniStatementRequest;
import com.fund_transfer.backend.dto.request.account.AccountSearchRequest;
import com.fund_transfer.backend.dto.request.account.AccountTransactionsRequest;
import com.fund_transfer.backend.dto.request.account.CreateAccountRequest;
import com.fund_transfer.backend.dto.request.account.UpdateAccountStatusRequest;
import com.fund_transfer.backend.dto.response.account.AccountBalanceResponse;
import com.fund_transfer.backend.dto.response.account.AccountDetailsResponse;
import com.fund_transfer.backend.dto.response.account.AccountMiniStatementResponse;
import com.fund_transfer.backend.dto.response.account.AccountResponse;
import com.fund_transfer.backend.dto.response.account.BeneficiaryResponse;
import com.fund_transfer.backend.dto.response.account.DebitCardResponse;
import com.fund_transfer.backend.dto.response.account.TransactionResponse;

import java.util.List;

public interface AccountService {

    PageResponse<AccountResponse> searchAccounts(AccountSearchRequest request);

    AccountDetailsResponse getAccountDetails(AccountDetailsRequest request);

    AccountBalanceResponse getAccountBalance(AccountBalanceRequest request);

    AccountMiniStatementResponse getMiniStatement(AccountMiniStatementRequest request);

    PageResponse<TransactionResponse> getTransactions(AccountTransactionsRequest request);

    List<BeneficiaryResponse> getBeneficiaries(AccountBeneficiariesRequest request);

    List<DebitCardResponse> getLinkedCards(AccountLinkedCardsRequest request);

    AccountResponse createAccount(CreateAccountRequest request);

    AccountResponse updateAccountStatus(String accountNumber, UpdateAccountStatusRequest request);

    AccountResponse getAccountByAccountNumber(String accountNumber);

    List<AccountResponse> getAccountsByCustomerId(String customerId);
}
