package com.fund_transfer.backend.controller;

import com.fund_transfer.backend.dto.common.ApiResponse;
import com.fund_transfer.backend.dto.common.PageResponse;
import com.fund_transfer.backend.dto.request.account.AccountBalanceRequest;
import com.fund_transfer.backend.dto.request.account.AccountBeneficiariesRequest;
import com.fund_transfer.backend.dto.request.account.AccountDetailsRequest;
import com.fund_transfer.backend.dto.request.account.AccountLinkedCardsRequest;
import com.fund_transfer.backend.dto.request.account.AccountMiniStatementRequest;
import com.fund_transfer.backend.dto.request.account.AccountSearchRequest;
import com.fund_transfer.backend.dto.request.account.AccountTransactionsRequest;
import com.fund_transfer.backend.dto.request.account.CreateAccountRequest;
import com.fund_transfer.backend.dto.response.account.AccountBalanceResponse;
import com.fund_transfer.backend.dto.response.account.AccountDetailsResponse;
import com.fund_transfer.backend.dto.response.account.AccountMiniStatementResponse;
import com.fund_transfer.backend.dto.response.account.AccountResponse;
import com.fund_transfer.backend.dto.response.account.BeneficiaryResponse;
import com.fund_transfer.backend.dto.response.account.DebitCardResponse;
import com.fund_transfer.backend.dto.response.account.TransactionResponse;
import com.fund_transfer.backend.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<AccountResponse>>> searchAccounts(
            @RequestBody(required = false) AccountSearchRequest request
    ) {
        log.info("REST request to search accounts");
        PageResponse<AccountResponse> response = accountService.searchAccounts(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/details")
    public ResponseEntity<ApiResponse<AccountDetailsResponse>> getAccountDetails(
            @Valid @RequestBody AccountDetailsRequest request
    ) {
        log.info("REST request to get account details for: {}", request.getAccountNumber());
        AccountDetailsResponse response = accountService.getAccountDetails(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/balance")
    public ResponseEntity<ApiResponse<AccountBalanceResponse>> getAccountBalance(
            @Valid @RequestBody AccountBalanceRequest request
    ) {
        log.info("REST request to get balance for: {}", request.getAccountNumber());
        AccountBalanceResponse response = accountService.getAccountBalance(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/mini-statement")
    public ResponseEntity<ApiResponse<AccountMiniStatementResponse>> getMiniStatement(
            @Valid @RequestBody AccountMiniStatementRequest request
    ) {
        log.info("REST request to get mini-statement for: {}", request.getAccountNumber());
        AccountMiniStatementResponse response = accountService.getMiniStatement(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/transactions")
    public ResponseEntity<ApiResponse<PageResponse<TransactionResponse>>> getTransactions(
            @Valid @RequestBody AccountTransactionsRequest request
    ) {
        log.info("REST request to get transactions for: {}", request.getAccountNumber());
        PageResponse<TransactionResponse> response = accountService.getTransactions(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/beneficiaries")
    public ResponseEntity<ApiResponse<List<BeneficiaryResponse>>> getBeneficiaries(
            @RequestBody(required = false) AccountBeneficiariesRequest request
    ) {
        log.info("REST request to get beneficiaries");
        List<BeneficiaryResponse> response = accountService.getBeneficiaries(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/linked-cards")
    public ResponseEntity<ApiResponse<List<DebitCardResponse>>> getLinkedCards(
            @Valid @RequestBody AccountLinkedCardsRequest request
    ) {
        log.info("REST request to get linked cards for: {}", request.getAccountNumber());
        List<DebitCardResponse> response = accountService.getLinkedCards(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<AccountResponse>> createAccount(
            @Valid @RequestBody CreateAccountRequest request
    ) {
        log.info("REST request to create account for customer: {}", request.getCustomerId());
        AccountResponse response = accountService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }
}
