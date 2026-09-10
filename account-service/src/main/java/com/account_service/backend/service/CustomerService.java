package com.account_service.backend.service;

import com.account_service.backend.dto.account.AccountDto.AccountResponse;
import com.account_service.backend.dto.common.PageResponse;
import com.account_service.backend.dto.customer.CustomerDto.*;

public interface CustomerService {

    SummaryResponse getCustomerSummary(CustomerRequest request);

    PageResponse<ProfileResponse> searchCustomers(SearchRequest request);

    ProfileResponse getCustomerDetails(CustomerRequest request);

    PageResponse<AccountResponse> getCustomerAccounts(AccountsRequest request);

    KycResponse getCustomerKycDetails(CustomerRequest request);

    RelationshipResponse getCustomerRelationshipDetails(CustomerRequest request);
}
