//package com.fund_transfer.backend.Cbs;
//
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.context.annotation.Profile;
//import org.springframework.stereotype.Component;
//
//import java.math.BigDecimal;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//@Profile({"prod", "uat", "staging"}) // active wherever the mock is NOT — adjust to your actual profile names
//public class CbsRestClient implements CbsClient {
//
////    private final WebClient cbsWebClient;
//    private final CbsProperties cbsProperties;
//
//    @Override
//    public BigDecimal getAvailableBalance(String ownerCif, String ownerAccountNumber) {
//        try {
//            CbsBalanceResponse response = cbsWebClient.get()
//                    .uri(cbsProperties.getBalanceInquiryPath(), ownerAccountNumber)
//                    .retrieve()
//                    .bodyToMono(CbsBalanceResponse.class)
//                    // Explicit timeout as a backstop even though the WebClient/HttpClient
//                    // already has connect/read timeouts configured — belt and suspenders.
//                    .timeout(Duration.ofMillis(cbsProperties.getReadTimeoutMs()))
//                    .block();
//
//            if (response == null || response.getAvailableBalance() == null) {
//                throw new CbsDebitException("CBS returned an empty balance response for account " + ownerAccountNumber);
//            }
//            return response.getAvailableBalance();
//
//        } catch (WebClientResponseException e) {
//            // CBS responded with a 4xx/5xx — log the body, it usually has a reason code.
//            log.error("CBS balance inquiry failed with status {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
//            throw new CbsDebitException("Failed to fetch balance from CBS: " + e.getStatusCode(), e);
//        } catch (WebClientRequestException e) {
//            // Network-level failure (connection refused, DNS, etc.) — distinct from
//            // a timeout, which surfaces differently (see debit() below for the
//            // timeout-specific handling you'll want to mirror here too).
//            log.error("CBS balance inquiry network error", e);
//            throw new CbsDebitException("Could not reach CBS for balance inquiry", e);
//        }
//    }
//
//    @Override
//    public String debit(String ownerCif, String ownerAccountNumber, BigDecimal amount, String idempotencyKey) {
//        return "";
//    }
//
//    @Override
//    public String debit(String ownerCif, String ownerAccountNumber, BigDecimal amount, String idempotencyKey) {
//        CbsDebitRequest requestBody = CbsDebitRequest.builder()
//                .cif(ownerCif)
//                .accountNumber(ownerAccountNumber)
//                .amount(amount)
//                .requestId(idempotencyKey)
//                .build();
//
//        try {
//            CbsDebitResponse response = cbsWebClient.post()
//                    .uri(cbsProperties.getDebitPath())
//                    .bodyValue(requestBody)
//                    .retrieve()
//                    .bodyToMono(CbsDebitResponse.class)
//                    .timeout(Duration.ofMillis(cbsProperties.getReadTimeoutMs()))
//                    .block();
//
//            if (response == null) {
//                throw new CbsDebitException("CBS returned an empty debit response");
//            }
//            if (!"SUCCESS".equalsIgnoreCase(response.getStatus())) {
//                throw new CbsDebitException(
//                        "CBS debit rejected: " + response.getReasonCode() + " - " + response.getReasonMessage());
//            }
//            return response.getDebitReference();
//
//        } catch (WebClientResponseException e) {
//            // Definite rejection (CBS responded, just with an error status).
//            // Safe to treat as "no money moved" — surface it as CbsDebitException.
//            log.error("CBS debit rejected, status {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
//            throw new CbsDebitException("CBS debit rejected with status " + e.getStatusCode(), e);
//
//        } catch (java.util.concurrent.TimeoutException | WebClientRequestException e) {
//            // *** THE IMPORTANT CASE FROM OUR EARLIER DISCUSSION ***
//            // CBS may or may not have processed the debit — we genuinely don't know.
//            // Do NOT return/throw a plain CbsDebitException here as if it's a clean
//            // failure, because TransactionService currently treats that as "safe to
//            // fail, nothing moved" (see Step 2 catch block). That assumption is
//            // WRONG on a timeout.
//            //
//            // Correct next step (not yet implemented — requires a CBS status-check
//            // endpoint, which you'll need to ask the CBS team for):
//            //   1. Call a GET /debit-status?requestId={idempotencyKey} endpoint
//            //   2. If CBS confirms SUCCESS -> treat as Step 2 success, continue flow
//            //   3. If CBS confirms FAILED/NOT_FOUND -> safe to treat as failure
//            //   4. If status check ALSO times out -> do not retry the debit; queue
//            //      for manual/scheduled reconciliation instead
//            //
//            // Until that status-check endpoint exists, at minimum log this loudly
//            // and distinguish it from a clean rejection so ops can investigate
//            // rather than silently reporting "transfer failed" to the customer
//            // when money may have actually moved.
//            log.error("CBS debit call TIMED OUT — outcome unknown, do not assume failure. " +
//                    "requestId={}, account={}", idempotencyKey, ownerAccountNumber, e);
//            throw new CbsDebitException(
//                    "CBS debit timed out — outcome unknown, requires status check before retry", e);
//        }
//    }
//
//    @Override
//    public void reverseDebit(String debitReference, BigDecimal amount, String idempotencyKey) {
//        CbsReversalRequest requestBody = CbsReversalRequest.builder()
//                .originalDebitReference(debitReference)
//                .amount(amount)
//                .requestId(idempotencyKey)
//                .build();
//
//        try {
//            CbsReversalResponse response = cbsWebClient.post()
//                    .uri(cbsProperties.getReversalPath())
//                    .bodyValue(requestBody)
//                    .retrieve()
//                    .bodyToMono(CbsReversalResponse.class)
//                    .timeout(Duration.ofMillis(cbsProperties.getReadTimeoutMs()))
//                    .block();
//
//            if (response == null || !"SUCCESS".equalsIgnoreCase(response.getStatus())) {
//                throw new CbsReversalException(
//                        "CBS reversal did not confirm success for debitReference=" + debitReference, null);
//            }
//
//        } catch (WebClientResponseException | WebClientRequestException e) {
//            // Any failure here — rejection OR timeout — must be treated as
//            // "reversal not confirmed", which is exactly what triggers Step 6
//            // (manual reconciliation escalation) in TransactionService. Unlike
//            // the debit call, there's no safe "assume nothing happened" reading
//            // here: we already know the debit succeeded, so an unconfirmed
//            // reversal always needs a human to check.
//            log.error("CBS reversal failed/unconfirmed for debitReference={}", debitReference, e);
//            throw new CbsReversalException("CBS reversal call failed for debitReference=" + debitReference, e);
//        }
//    }
//}
