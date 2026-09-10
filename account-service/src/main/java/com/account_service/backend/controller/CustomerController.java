package com.account_service.backend.controller;

import com.account_service.backend.dto.account.AccountDto.AccountResponse;
import com.account_service.backend.dto.common.ApiResponse;
import com.account_service.backend.dto.common.PageResponse;
import com.account_service.backend.dto.customer.CustomerDto.*;
import com.account_service.backend.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping({"/api/v1/customer/summary", "/api/v1/customers/summary"})
    public ResponseEntity<ApiResponse<SummaryResponse>> getCustomerSummary(
            @RequestBody(required = false) CustomerRequest request
    ) {
        log.info("REST request to fetch customer summary");
        SummaryResponse response = customerService.getCustomerSummary(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/api/v1/customers/search")
    public ResponseEntity<ApiResponse<PageResponse<ProfileResponse>>> searchCustomers(
            @RequestBody(required = false) SearchRequest request
    ) {
        log.info("REST request to search customers");
        PageResponse<ProfileResponse> response = customerService.searchCustomers(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/api/v1/customers/details")
    public ResponseEntity<ApiResponse<ProfileResponse>> getCustomerDetails(
            @RequestBody(required = false) CustomerRequest request
    ) {
        log.info("REST request to fetch customer details");
        ProfileResponse response = customerService.getCustomerDetails(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/api/v1/customers/accounts")
    public ResponseEntity<ApiResponse<PageResponse<AccountResponse>>> getCustomerAccounts(
            @RequestBody(required = false) AccountsRequest request
    ) {
        log.info("REST request to fetch customer accounts");
        PageResponse<AccountResponse> response = customerService.getCustomerAccounts(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/api/v1/customers/kyc-details")
    public ResponseEntity<ApiResponse<KycResponse>> getCustomerKycDetails(
            @RequestBody(required = false) CustomerRequest request
    ) {
        log.info("REST request to fetch customer KYC details");
        KycResponse response = customerService.getCustomerKycDetails(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/api/v1/customers/relationship-details")
    public ResponseEntity<ApiResponse<RelationshipResponse>> getCustomerRelationshipDetails(
            @RequestBody(required = false) CustomerRequest request
    ) {
        log.info("REST request to fetch customer relationship details");
        RelationshipResponse response = customerService.getCustomerRelationshipDetails(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
