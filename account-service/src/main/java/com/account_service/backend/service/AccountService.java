package com.account_service.backend.service;

import com.account_service.backend.dto.account.AccountDto.*;
import com.account_service.backend.dto.common.PageResponse;

import java.util.List;

public interface AccountService {

    PageResponse<AccountResponse> searchAccounts(SearchRequest request);

    DetailsResponse getAccountDetails(DetailsRequest request);

    BalanceResponse getAccountBalance(BalanceRequest request);

    MiniStatementResponse getMiniStatement(MiniStatementRequest request);

    PageResponse<TransactionResponse> getTransactions(TransactionsRequest request);

    List<BeneficiaryResponse> getBeneficiaries(BeneficiariesRequest request);

    List<DebitCardResponse> getLinkedCards(LinkedCardsRequest request);

    AccountResponse createAccount(CreateRequest request);

    AccountResponse updateAccountStatus(String accountNumber, UpdateStatusRequest request);

    AccountResponse getAccountByAccountNumber(String accountNumber);

    List<AccountResponse> getAccountsByCustomerId(String customerId);
}
