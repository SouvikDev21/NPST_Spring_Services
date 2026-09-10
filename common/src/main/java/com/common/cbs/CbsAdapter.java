package com.common.cbs;

import com.common.cbs.dto.CbsAccountDetailsResponse;
import com.common.cbs.dto.CbsCardInquiryResponse;
import com.common.cbs.dto.CbsCustomerInquiryResponse;
import com.common.cbs.dto.CbsStatementResponse;

import java.util.Optional;

public interface CbsAdapter {

    Optional<CbsCustomerInquiryResponse> getCustomerAccounts(String customerId);

    Optional<CbsAccountDetailsResponse> getAccountDetails(String accountId);

    Optional<CbsStatementResponse> getAccountStatements(String accountId, String fromDate, String toDate, Integer offset, Integer limit);

    Optional<CbsCardInquiryResponse> getLinkedCards(String accountId);
}
