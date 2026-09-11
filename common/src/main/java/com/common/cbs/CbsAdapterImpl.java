package com.common.cbs;

import com.common.cbs.dto.CbsAccountDetailsRequest;
import com.common.cbs.dto.CbsAccountDetailsResponse;
import com.common.cbs.dto.CbsCardInquiryRequest;
import com.common.cbs.dto.CbsCardInquiryResponse;
import com.common.cbs.dto.CbsCustomerInquiryRequest;
import com.common.cbs.dto.CbsCustomerInquiryResponse;
import com.common.cbs.dto.CbsStatementRequest;
import com.common.cbs.dto.CbsStatementResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Component
public class CbsAdapterImpl implements CbsAdapter {

    private final RestClient restClient;
    private final CbsProperties cbsProperties;

    public CbsAdapterImpl(CbsProperties cbsProperties, RestClient.Builder restClientBuilder) {
        this.cbsProperties = cbsProperties;
        String rawBaseUrl = cbsProperties.getBaseUrl() != null ? cbsProperties.getBaseUrl().trim() : "";
        String baseUrl = normalizeBaseUrl(rawBaseUrl);
        log.info("Initializing CBS Adapter with base URL: {}", baseUrl);

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        if (cbsProperties.getConnectTimeoutMs() != null) {
            requestFactory.setConnectTimeout(Duration.ofMillis(cbsProperties.getConnectTimeoutMs()));
        }
        if (cbsProperties.getReadTimeoutMs() != null) {
            requestFactory.setReadTimeout(Duration.ofMillis(cbsProperties.getReadTimeoutMs()));
        }

        RestClient.Builder builder = restClientBuilder.requestFactory(requestFactory);
        if (!baseUrl.isBlank()) {
            builder.baseUrl(baseUrl);
        }
        builder.defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        if (cbsProperties.getChannelId() != null) {
            builder.defaultHeader(CbsConstants.HEADER_CHANNEL_ID, cbsProperties.getChannelId());
        }
        if (cbsProperties.getUserId() != null) {
            builder.defaultHeader(CbsConstants.HEADER_USER_ID, cbsProperties.getUserId());
        }
        if (cbsProperties.getBranchCode() != null) {
            builder.defaultHeader(CbsConstants.HEADER_BRANCH_CODE, cbsProperties.getBranchCode());
        }

        this.restClient = builder.build();
    }

    public static String normalizeBaseUrl(String rawBaseUrl) {
        if (rawBaseUrl == null || rawBaseUrl.isBlank()) {
            return "http://103.209.145.243:9101/cbs/v3";
        }
        String url = rawBaseUrl.trim().replaceAll("/+$", "");
        if (url.contains("/mock/cbs")) {
            url = url.replace("/mock/cbs", "/cbs/v3");
        }
        if (!url.endsWith("/cbs/v3") && !url.contains("/cbs/v3")) {
            url = url + "/cbs/v3";
        }
        return url;
    }

    public static String resolvePath(String path) {
        if (path == null) {
            return "/";
        }
        String p = path.trim();
        if (p.startsWith("/api/v3/")) {
            p = p.substring("/api/v3".length());
        } else if (p.startsWith("/cbs/v3/")) {
            p = p.substring("/cbs/v3".length());
        }
        if (!p.startsWith("/")) {
            p = "/" + p;
        }
        return p;
    }

    @Override
    public Optional<CbsCustomerInquiryResponse> getCustomerAccounts(String customerId) {
        try {
            log.info("Calling CBS customer inquiry for CIF: {}", customerId);
            CbsCustomerInquiryResponse response = restClient.post()
                    .uri(resolvePath(CbsConstants.PATH_V3_CUSTOMER_ACCOUNTS_INQUIRY))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new CbsCustomerInquiryRequest(customerId))
                    .retrieve()
                    .body(CbsCustomerInquiryResponse.class);
            return Optional.ofNullable(response);
        } catch (RestClientResponseException rx) {
            log.error("Failed to query CBS customer accounts for CIF {}: HTTP {} - {}", customerId, rx.getStatusCode(), rx.getResponseBodyAsString());
            return Optional.empty();
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
                    .uri(resolvePath(CbsConstants.PATH_V3_ACCOUNT_DETAILS_INQUIRY))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new CbsAccountDetailsRequest(accountId))
                    .retrieve()
                    .body(CbsAccountDetailsResponse.class);
            return Optional.ofNullable(response);
        } catch (RestClientResponseException rx) {
            log.error("Failed to query CBS account details for {}: HTTP {} - {}", accountId, rx.getStatusCode(), rx.getResponseBodyAsString());
            return Optional.empty();
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
                    .uri(resolvePath(CbsConstants.PATH_V3_ACCOUNT_STATEMENTS))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(CbsStatementResponse.class);
            return Optional.ofNullable(response);
        } catch (RestClientResponseException rx) {
            log.error("Failed to query CBS account statements for {}: HTTP {} - {}", accountId, rx.getStatusCode(), rx.getResponseBodyAsString());
            return Optional.empty();
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
                    .uri(resolvePath(CbsConstants.PATH_V3_CARDS_INQUIRY))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new CbsCardInquiryRequest(accountId))
                    .retrieve()
                    .body(CbsCardInquiryResponse.class);
            return Optional.ofNullable(response);
        } catch (RestClientResponseException rx) {
            log.error("Failed to query CBS cards for account {}: HTTP {} - {}", accountId, rx.getStatusCode(), rx.getResponseBodyAsString());
            return Optional.empty();
        } catch (Exception e) {
            log.error("Failed to query CBS cards for account {}: {}", accountId, e.getMessage());
            return Optional.empty();
        }
    }
}
