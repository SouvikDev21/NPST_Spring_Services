package com.fund_transfer.backend.controller;

import com.fund_transfer.backend.dto.common.ApiResponse;
import com.fund_transfer.backend.dto.common.PageResponse;
import com.fund_transfer.backend.dto.request.customer.CustomerAccountsRequest;
import com.fund_transfer.backend.dto.request.customer.CustomerDetailsRequest;
import com.fund_transfer.backend.dto.request.customer.CustomerKycDetailsRequest;
import com.fund_transfer.backend.dto.request.customer.CustomerRelationshipDetailsRequest;
import com.fund_transfer.backend.dto.request.customer.CustomerSearchRequest;
import com.fund_transfer.backend.dto.request.customer.CustomerSummaryRequest;
import com.fund_transfer.backend.dto.response.account.AccountResponse;
import com.fund_transfer.backend.dto.response.customer.CustomerKycDetailsResponse;
import com.fund_transfer.backend.dto.response.customer.CustomerProfileResponse;
import com.fund_transfer.backend.dto.response.customer.CustomerRelationshipDetailsResponse;
import com.fund_transfer.backend.dto.response.customer.CustomerSummaryResponse;
import com.fund_transfer.backend.service.CustomerService;
import jakarta.validation.Valid;
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
    public ResponseEntity<ApiResponse<CustomerSummaryResponse>> getCustomerSummary(
            @RequestBody(required = false) CustomerSummaryRequest request
    ) {
        log.info("REST request to fetch customer summary");
        CustomerSummaryResponse response = customerService.getCustomerSummary(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/api/v1/customers/search")
    public ResponseEntity<ApiResponse<PageResponse<CustomerProfileResponse>>> searchCustomers(
            @RequestBody(required = false) CustomerSearchRequest request
    ) {
        log.info("REST request to search customers");
        PageResponse<CustomerProfileResponse> response = customerService.searchCustomers(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/api/v1/customers/details")
    public ResponseEntity<ApiResponse<CustomerProfileResponse>> getCustomerDetails(
            @RequestBody(required = false) CustomerDetailsRequest request
    ) {
        log.info("REST request to fetch customer details");
        CustomerProfileResponse response = customerService.getCustomerDetails(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/api/v1/customers/accounts")
    public ResponseEntity<ApiResponse<PageResponse<AccountResponse>>> getCustomerAccounts(
            @RequestBody(required = false) CustomerAccountsRequest request
    ) {
        log.info("REST request to fetch customer accounts");
        PageResponse<AccountResponse> response = customerService.getCustomerAccounts(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/api/v1/customers/kyc-details")
    public ResponseEntity<ApiResponse<CustomerKycDetailsResponse>> getCustomerKycDetails(
            @RequestBody(required = false) CustomerKycDetailsRequest request
    ) {
        log.info("REST request to fetch customer KYC details");
        CustomerKycDetailsResponse response = customerService.getCustomerKycDetails(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/api/v1/customers/relationship-details")
    public ResponseEntity<ApiResponse<CustomerRelationshipDetailsResponse>> getCustomerRelationshipDetails(
            @RequestBody(required = false) CustomerRelationshipDetailsRequest request
    ) {
        log.info("REST request to fetch customer relationship details");
        CustomerRelationshipDetailsResponse response = customerService.getCustomerRelationshipDetails(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
