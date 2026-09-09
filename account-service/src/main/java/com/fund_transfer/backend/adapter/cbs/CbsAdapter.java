package com.fund_transfer.backend.adapter.cbs;

import com.fund_transfer.backend.adapter.cbs.dto.*;

import java.util.Optional;

public interface CbsAdapter {

    Optional<CbsCustomerInquiryResponse> getCustomerAccounts(String customerId);

    Optional<CbsAccountDetailsResponse> getAccountDetails(String accountId);

    Optional<CbsStatementResponse> getAccountStatements(String accountId, String fromDate, String toDate, Integer offset, Integer limit);

    Optional<CbsCardInquiryResponse> getLinkedCards(String accountId);
}
