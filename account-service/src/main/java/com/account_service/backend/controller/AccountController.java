package com.account_service.backend.controller;

import com.account_service.backend.dto.account.AccountDto.*;
import com.account_service.backend.dto.common.ApiResponse;
import com.account_service.backend.dto.common.PageResponse;
import com.account_service.backend.service.AccountService;
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
            @RequestBody(required = false) SearchRequest request
    ) {
        log.info("REST request to search accounts");
        PageResponse<AccountResponse> response = accountService.searchAccounts(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/details")
    public ResponseEntity<ApiResponse<DetailsResponse>> getAccountDetails(
            @Valid @RequestBody DetailsRequest request
    ) {
        log.info("REST request to get account details for: {}", request.getAccountNumber());
        DetailsResponse response = accountService.getAccountDetails(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/balance")
    public ResponseEntity<ApiResponse<BalanceResponse>> getAccountBalance(
            @Valid @RequestBody BalanceRequest request
    ) {
        log.info("REST request to get balance for: {}", request.getAccountNumber());
        BalanceResponse response = accountService.getAccountBalance(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/mini-statement")
    public ResponseEntity<ApiResponse<MiniStatementResponse>> getMiniStatement(
            @Valid @RequestBody MiniStatementRequest request
    ) {
        log.info("REST request to get mini-statement for: {}", request.getAccountNumber());
        MiniStatementResponse response = accountService.getMiniStatement(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/transactions")
    public ResponseEntity<ApiResponse<PageResponse<TransactionResponse>>> getTransactions(
            @Valid @RequestBody TransactionsRequest request
    ) {
        log.info("REST request to get transactions for: {}", request.getAccountNumber());
        PageResponse<TransactionResponse> response = accountService.getTransactions(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/beneficiaries")
    public ResponseEntity<ApiResponse<List<BeneficiaryResponse>>> getBeneficiaries(
            @RequestBody(required = false) BeneficiariesRequest request
    ) {
        log.info("REST request to get beneficiaries");
        List<BeneficiaryResponse> response = accountService.getBeneficiaries(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/linked-cards")
    public ResponseEntity<ApiResponse<List<DebitCardResponse>>> getLinkedCards(
            @Valid @RequestBody LinkedCardsRequest request
    ) {
        log.info("REST request to get linked cards for: {}", request.getAccountNumber());
        List<DebitCardResponse> response = accountService.getLinkedCards(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<AccountResponse>> createAccount(
            @Valid @RequestBody CreateRequest request
    ) {
        log.info("REST request to create account for customer: {}", request.getCustomerId());
        AccountResponse response = accountService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }
}
