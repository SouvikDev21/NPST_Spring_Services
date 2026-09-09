package com.fund_transfer.backend.adapter.cbs;

import com.fund_transfer.backend.adapter.cbs.dto.*;
import com.fund_transfer.backend.common.constants.CbsConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Optional;

@Slf4j
@Component
public class CbsAdapterImpl implements CbsAdapter {

    private final RestClient restClient;

    public CbsAdapterImpl(
            @Value("${cbs.base-url:http://103.209.145.243:9101}") String baseUrl,
            @Value("${cbs.channel-id:INTERNET_BANKING}") String channelId,
            @Value("${cbs.user-id:APIUSER}") String userId,
            @Value("${cbs.branch-code:001}") String branchCode,
            RestClient.Builder restClientBuilder
    ) {
        log.info("Initializing CBS Adapter with base URL: {}", baseUrl);
        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
                .defaultHeader(CbsConstants.HEADER_CHANNEL_ID, channelId)
                .defaultHeader(CbsConstants.HEADER_USER_ID, userId)
                .defaultHeader(CbsConstants.HEADER_BRANCH_CODE, branchCode)
                .build();
    }

    @Override
    public Optional<CbsCustomerInquiryResponse> getCustomerAccounts(String customerId) {
        try {
            log.info("Calling CBS customer inquiry for CIF: {}", customerId);
            CbsCustomerInquiryResponse response = restClient.post()
                    .uri(CbsConstants.PATH_V3_CUSTOMER_ACCOUNTS_INQUIRY)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new CbsCustomerInquiryRequest(customerId))
                    .retrieve()
                    .body(CbsCustomerInquiryResponse.class);
            return Optional.ofNullable(response);
        } catch (Exception e) {
            log.error("Failed to query CBS customer accounts for CIF {}: {}", customerId, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public Optional<CbsAccountDetailsResponse> getAccountDetails(String accountId) {
        try {
            log.info("Calling CBS account details inquiry for AccountId: {}", accountId);
            CbsAccountDetailsResponse response = restClient.post()
                    .uri(CbsConstants.PATH_V3_ACCOUNT_DETAILS_INQUIRY)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new CbsAccountDetailsRequest(accountId))
                    .retrieve()
                    .body(CbsAccountDetailsResponse.class);
            return Optional.ofNullable(response);
        } catch (Exception e) {
            log.error("Failed to query CBS account details for {}: {}", accountId, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public Optional<CbsStatementResponse> getAccountStatements(String accountId, String fromDate, String toDate, Integer offset, Integer limit) {
        try {
            log.info("Calling CBS statements for AccountId: {}, date range: {} to {}", accountId, fromDate, toDate);
            CbsStatementRequest request = CbsStatementRequest.builder()
                    .accountId(accountId)
                    .fromDate(fromDate)
                    .toDate(toDate)
                    .offset(offset != null ? offset : 0)
                    .limit(limit != null ? limit : 10)
                    .build();

            CbsStatementResponse response = restClient.post()
                    .uri(CbsConstants.PATH_V3_ACCOUNT_STATEMENTS)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(CbsStatementResponse.class);
            return Optional.ofNullable(response);
        } catch (Exception e) {
            log.error("Failed to query CBS account statements for {}: {}", accountId, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public Optional<CbsCardInquiryResponse> getLinkedCards(String accountId) {
        try {
            log.info("Calling CBS cards inquiry for AccountId: {}", accountId);
            CbsCardInquiryResponse response = restClient.post()
                    .uri(CbsConstants.PATH_V3_CARDS_INQUIRY)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new CbsCardInquiryRequest(accountId))
                    .retrieve()
                    .body(CbsCardInquiryResponse.class);
            return Optional.ofNullable(response);
        } catch (Exception e) {
            log.error("Failed to query CBS cards for account {}: {}", accountId, e.getMessage());
            return Optional.empty();
        }
    }
}
