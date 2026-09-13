package com.fund_transfer.backend.Cbs;


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

    // Plain, synchronous RestClient bean — no Mono, no .block(), no reactive
    // operators. Each call below runs top-to-bottom like ordinary Java code
    // and either returns a value or throws, matching how TransactionService
    // is already written.
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

            JsonNode accountNode = rawResponse.path("account");

            if (accountNode.isMissingNode() || accountNode.path("availableBalance").isMissingNode()) {
                throw new CbsDebitException("CBS returned an empty balance response for account " + ownerAccountNumber);
            }

            String accountNumber = accountNode.path("accountId").asText();
            BigDecimal availableBalance = accountNode.path("availableBalance").decimalValue();
            String currency = accountNode.path("currency").asText();

            CbsBalanceResponse response = new CbsBalanceResponse(accountNumber, availableBalance, currency);

            return response.AvailableBalance();

        }catch (RestClientResponseException e) {
            // CBS responded, just with a 4xx/5xx status — a definite answer, not an unknown.
            log.error("CBS balance inquiry failed with status {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new CbsDebitException("Failed to fetch balance from CBS: " + e.getStatusCode(), e);

        } catch (ResourceAccessException e) {
            // RestClient's umbrella for connection failures AND timeouts (both
            // connect-timeout and read-timeout land here, wrapping an underlying
            // IOException/SocketTimeoutException). For balance inquiry this is
            // low-risk either way — no money has moved yet regardless of outcome —
            // so treating it as a plain failure is fine here, unlike in debit() below.
            log.error("CBS balance inquiry network/timeout error", e);
            throw new CbsDebitException("Could not reach CBS for balance inquiry", e);
        }

    }

    @Override
    public String debit(String ownerAccountNumber, BigDecimal amount, String idempotencyKey) {
        // Plain records don't have @Builder — call the canonical constructor
        // directly, in the order fields were declared.
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
            // Definite rejection (CBS responded, just with an error status).
            // Safe to treat as "no money moved" here.
            log.error("CBS debit rejected, status {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            //throw new CbsDebitException("CBS debit rejected with status " + e.getStatusCode(), e);

        } catch (ResourceAccessException e) {
            // *** THE IMPORTANT CASE FROM OUR EARLIER DISCUSSION ***
            // This fires on BOTH connect timeouts (never reached CBS — safe to
            // treat as failed, nothing happened) AND response/read timeouts
            // (request reached CBS, we just never got the reply — outcome unknown).
            // RestClient doesn't distinguish these for you, so we check the
            // cause chain to tell them apart:
            boolean likelyReachedServer = isReadTimeout(e);

            if (likelyReachedServer) {
                log.error("CBS debit call TIMED OUT WAITING FOR RESPONSE — outcome unknown, do NOT assume " +
                        "failure and do NOT blindly retry. requestId={}, account={}", idempotencyKey, ownerAccountNumber, e);
                // Correct next step (not yet implemented — requires a CBS status-check
                // endpoint, which you'll need to ask the CBS team for):
                //   1. Call GET /debit-status?requestId={idempotencyKey}
                //   2. CBS confirms SUCCESS -> treat as Step 2 success, continue flow
                //   3. CBS confirms FAILED/NOT_FOUND -> safe to treat as failure
//                //   4. Status check ALSO fails -> do not retry; queue for manual reconciliation
//                throw new CbsDebitException(
//                        "CBS debit timed out waiting for response — outcome unknown, requires status check before retry", e);
            } else {
                // Never even connected (DNS failure, connection refused, connect-timeout).
                // Genuinely safe to treat as "nothing happened."
//                log.error("CBS debit call could not connect to CBS — safe to treat as failed. requestId={}", idempotencyKey, e);
//                throw new CbsDebitException("Could not reach CBS to process debit", e);
            }
        }
        return  "Failed";
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
//                throw new CbsReversalException(
//                        "CBS reversal did not confirm success for debitReference=" + debitReference, null);
            }

        } catch (RestClientResponseException | ResourceAccessException e) {
            // Any failure here — rejection, timeout, or connection error — is
            // treated as "reversal not confirmed." Unlike debit(), there's no
            // safe "assume nothing happened" branch: the debit already
            // succeeded, so any unconfirmed reversal outcome must escalate to
            // manual reconciliation (Step 6 in TransactionService), regardless
            // of which failure mode caused it.
            log.error("CBS reversal failed/unconfirmed for debitReference={}", debitReference, e);
            //throw new CbsReversalException("CBS reversal call failed for debitReference=" + debitReference, e);
        }
    }

    // Best-effort check to tell a connect-level failure apart from a
    // response/read timeout, by inspecting the wrapped cause. Exact exception
    // types can vary slightly depending on which HTTP client backs RestClient
    // (Apache HttpClient 5 here) — verify against real timeout behavior in a
    // test against your CBS server (or a local stub) before relying on this
    // distinction in production.
    private boolean isReadTimeout(ResourceAccessException e) {
        Throwable cause = e.getCause();
        return cause instanceof SocketTimeoutException
                || (cause != null && cause.getClass().getSimpleName().toLowerCase().contains("responsetimeout"));
    }
}
