package com.fund_transfer.backend.service;

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

public interface CustomerService {

    CustomerSummaryResponse getCustomerSummary(CustomerSummaryRequest request);

    PageResponse<CustomerProfileResponse> searchCustomers(CustomerSearchRequest request);

    CustomerProfileResponse getCustomerDetails(CustomerDetailsRequest request);

    PageResponse<AccountResponse> getCustomerAccounts(CustomerAccountsRequest request);

    CustomerKycDetailsResponse getCustomerKycDetails(CustomerKycDetailsRequest request);

    CustomerRelationshipDetailsResponse getCustomerRelationshipDetails(CustomerRelationshipDetailsRequest request);
}
