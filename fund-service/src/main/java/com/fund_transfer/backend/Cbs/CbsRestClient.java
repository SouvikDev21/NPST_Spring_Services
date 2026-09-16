package com.fund_transfer.backend.Cbs;


import com.fund_transfer.backend.exception.CbsReversalException;
import tools.jackson.databind.JsonNode;
import com.fund_transfer.backend.dto.Request.CbsDebitRequest;
import com.fund_transfer.backend.dto.Request.CbsReversalRequest;
import com.fund_transfer.backend.dto.Response.CbsBalanceResponse;
import com.fund_transfer.backend.dto.Response.CbsDebitResponse;
import com.fund_transfer.backend.dto.Response.CbsReversalResponse;
import com.fund_transfer.backend.exception.CbsDebitException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.jackson.autoconfigure.JacksonProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.SocketTimeoutException;


@Slf4j
@Component
@RequiredArgsConstructor
@Profile({"prod", "uat", "local"}) // active wherever the mock is NOT — adjust to your actual profile names
public class CbsRestClient implements CbsClient {

    private final RestClient cbsRestClient;
    private final CbsProperties cbsProperties;

    @Override
    public BigDecimal getAvailableBalance( String ownerAccountNumber) {
        try {
            JsonNode rawResponse = cbsRestClient.get()
                    .uri(cbsProperties.getBalanceInquiryPath(), ownerAccountNumber)
                    .retrieve()
                    .body(JsonNode.class);

            log.info("CBS raw response: {}", rawResponse.toPrettyString());

            JsonNode accountNode = rawResponse.path("Account");

            if (accountNode.isMissingNode() || accountNode.path("AvailableBalance").isMissingNode()) {
                throw new CbsDebitException("CBS returned an empty balance response for account " + ownerAccountNumber);
            }

            String accountNumber = accountNode.path("AccountNumber").asText();
            BigDecimal availableBalance = accountNode.path("AvailableBalance").decimalValue();
            String currency = accountNode.path("Currency").asText();

            CbsBalanceResponse response = new CbsBalanceResponse(accountNumber, availableBalance, currency);

            return response.AvailableBalance();

        }catch (RestClientResponseException e) {

            log.error("CBS balance inquiry failed with status {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new CbsDebitException("Failed to fetch balance from CBS: " + e.getStatusCode(), e);

        } catch (ResourceAccessException e) {

            log.error("CBS balance inquiry network/timeout error", e);
            throw new CbsDebitException("Could not reach CBS for balance inquiry", e);
        }

    }

    @Override
    public String debit(String ownerAccountNumber, BigInteger amount, String idempotencyKey) {

        CbsDebitRequest requestBody = new CbsDebitRequest( ownerAccountNumber, amount, idempotencyKey);

        try {
            CbsDebitResponse response = cbsRestClient.post()
                    .uri(cbsProperties.getDebitPath())
                    .body(requestBody)
                    .retrieve()
                    .body(CbsDebitResponse.class);

            if (response == null) {
                throw new CbsDebitException("CBS returned an empty debit response");
            }
            if (!"SUCCESS".equalsIgnoreCase(response.status())) {
                throw new CbsDebitException(
                        "CBS debit rejected: " + response.reasonCode() + " - " + response.reasonMessage());
            }
            return response.debitReference();

        } catch (RestClientResponseException e) {

            log.error("CBS debit rejected, status {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new CbsDebitException("CBS debit rejected with status " + e.getStatusCode(), e);

        } catch (ResourceAccessException e) {

            boolean likelyReachedServer = isReadTimeout(e);

            if (likelyReachedServer) {
                log.error("CBS debit call TIMED OUT WAITING FOR RESPONSE — outcome unknown, do NOT assume " +
                        "failure and do NOT blindly retry. requestId={}, account={}", idempotencyKey, ownerAccountNumber, e);

                throw new CbsDebitException(
                        "CBS debit timed out waiting for response — outcome unknown, requires status check before retry", e);
            } else {
                log.error("CBS debit call could not connect to CBS — safe to treat as failed. requestId={}", idempotencyKey, e);
                throw new CbsDebitException("Could not reach CBS to process debit", e);
            }
        }
    }

    @Override
    public void reverseDebit(String debitReference, BigInteger amount, String idempotencyKey) {
        CbsReversalRequest requestBody = new CbsReversalRequest(debitReference, amount, idempotencyKey);

        try {
            CbsReversalResponse response = cbsRestClient.post()
                    .uri(cbsProperties.getReversalPath())
                    .body(requestBody)
                    .retrieve()
                    .body(CbsReversalResponse.class);

            if (response == null || !"SUCCESS".equalsIgnoreCase(response.status())) {
                throw new CbsReversalException(
                        "CBS reversal did not confirm success for debitReference=" + debitReference, null);
            }

        } catch (RestClientResponseException | ResourceAccessException e) {
            log.error("CBS reversal failed/unconfirmed for debitReference={}", debitReference, e);
            throw new CbsReversalException("CBS reversal call failed for debitReference=" + debitReference, e);
        }
    }


    private boolean isReadTimeout(ResourceAccessException e) {
        Throwable cause = e.getCause();
        return cause instanceof SocketTimeoutException
                || (cause != null && cause.getClass().getSimpleName().toLowerCase().contains("responsetimeout"));
    }
}
