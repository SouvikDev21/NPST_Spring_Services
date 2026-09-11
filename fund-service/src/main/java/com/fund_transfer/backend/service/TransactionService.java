package com.fund_transfer.backend.service;

import com.fund_transfer.backend.Cbs.CbsClient;
import com.fund_transfer.backend.Npci.NpciClient;

import com.fund_transfer.backend.dto.Request.TransferRequest;
import com.fund_transfer.backend.dto.Response.TransactionResponse;
import com.fund_transfer.backend.enums.TransactionStatus;
import com.fund_transfer.backend.exception.CbsDebitException;
import com.fund_transfer.backend.exception.CbsReversalException;
import com.fund_transfer.backend.exception.InsufficientBalanceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final CbsClient cbsClient;
    private final NpciClient npciClient;
    // private final TransactionRepository transactionRepository; // persist state at every step — see note at bottom

    public TransactionResponse processTransfer(TransferRequest request) {

        // ---------------------------------------------------------------
        // STEP 1: Validate — check balance BEFORE touching any money.
        // Nothing has moved yet, so if this fails we can reject cleanly
        // with no cleanup required.
        // ---------------------------------------------------------------
        BigDecimal availableBalance = cbsClient.getAvailableBalance(
              request.InitiatorAccountNumber());

        if (availableBalance.compareTo(request.amountMinorUnits()) < 0) {
            // Un-commented — this was silently swallowing insufficient-balance
            // requests before, letting execution fall through to Step 2 and
            // attempt a debit CBS should never see.
            throw new InsufficientBalanceException(
                    "Available balance " + availableBalance + " is less than transfer amount " + request.amountMinorUnits());
        }
        // At this point you'd normally also persist a transaction row with
        // status = INITIATED (or VALIDATED), so even a crash right after this
        // line leaves an auditable record. Left as a comment since it needs
        // your TransactionRepository / entity, not shown here.
        log.info("Step 1 complete: balance validated for account {}", request.InitiatorAccountNumber());

        // ---------------------------------------------------------------
        // STEP 2: Debit the owner's account at CBS.
        // idempotencyKey ensures a retried HTTP request (e.g. UI double-click,
        // client timeout+retry) doesn't cause a second debit — CBS (or our
        // MockCbsClient) recognizes the key and returns the same reference.
        // ---------------------------------------------------------------
        String debitReference;
        try {
            debitReference = cbsClient.debit(
                    request.InitiatorAccountNumber(),
                    request.amountMinorUnits(),
                    request.idempotencyKey());
        } catch (CbsDebitException e) {
            // Definite rejection from CBS (e.g. insufficient funds detected
            // server-side even though our pre-check passed, frozen account).
            // No money moved — safe to fail the whole transfer here.
            log.warn("Step 2 failed: CBS debit rejected — {}", e.getMessage());
            return buildResponse(request, TransactionStatus.FAILED, e.getMessage());
        }
        // NOTE on timeouts: if the CBS call times out (no exception caught
        // above, but no response either — depends on your HTTP client config),
        // do NOT assume the debit failed. Query CBS for the status of this
        // idempotencyKey/debitReference before deciding. Retrying the debit
        // call blindly on a timeout risks a double debit if CBS actually
        // processed it. A real CbsRestClient implementation must handle
        // this explicitly rather than throwing a plain CbsDebitException.
        log.info("Step 2 complete: CBS debit succeeded, reference={}", debitReference);

        // ---------------------------------------------------------------
        // STEP 3: Mock the NPCI disbursal request to the beneficiary.
        // This is the "external network" leg — money has already left the
        // owner's account (Step 2), but hasn't reached the beneficiary yet.
        // ---------------------------------------------------------------
        boolean npciSuccess;
        try {
            npciSuccess = npciClient.disburse(
                    request.destinationAccountNumber(),
                    request.beneficiaryIfsc(),
                    request.amountMinorUnits(),
                    request.idempotencyKey());
        } catch (Exception e) {
            // Treat an NPCI exception (timeout, network error) the SAME as an
            // explicit failure for now, since our flow only distinguishes
            // success/failure. In production, an ambiguous NPCI outcome
            // should route to a reconciliation/status-poll job instead of
            // immediately reversing — reversing after a false "failure" you
            // can't yet confirm risks reversing a transfer NPCI actually
            // completed.
            log.warn("Step 3: NPCI call threw an exception, treating as failure — {}", e.getMessage());
            npciSuccess = false;
        }

        // ---------------------------------------------------------------
        // STEP 4: Based on the NPCI response, set transaction status.
        // ---------------------------------------------------------------
        if (npciSuccess) {
            log.info("Step 4: NPCI disbursal succeeded for {}", request.destinationAccountNumber());
            // Persist status = SUCCESS here (transactionRepository.save(...)).
            return buildResponse(request, TransactionStatus.SUCCESS, null);
        }

        // ---------------------------------------------------------------
        // STEP 5: NPCI failed AFTER the CBS debit succeeded — money left the
        // owner's account but never reached the beneficiary. We must reverse
        // the debit (compensating transaction / saga rollback) so the owner
        // isn't left out of pocket.
        // ---------------------------------------------------------------
        log.warn("Step 4/5: NPCI disbursal failed, reversing CBS debit reference={}", debitReference);
        try {
            // Was request.amount() — that method no longer exists on
            // InitiateTransferRequest; corrected to amountMinorUnits() to
            // match every other call site in this method.
            cbsClient.reverseDebit(debitReference, request.amountMinorUnits(), request.idempotencyKey());
            log.info("Step 5 complete: reversal succeeded for reference={}", debitReference);
            // Persist status = REVERSED.
            return buildResponse(request, TransactionStatus.NPCI_FAILED, "Disbursal failed; amount reversed to owner account");
        } catch (CbsReversalException reversalEx) {
            // ---------------------------------------------------------------
            // STEP 6: Worst case — debit succeeded, disbursal failed, AND
            // reversal failed. The owner has been debited with no beneficiary
            // credit and no refund. This must never be silently swallowed:
            // persist status, alert on-call/ops, and push to a manual
            // reconciliation queue for someone to fix by hand.
            // ---------------------------------------------------------------
            log.error("Step 6: CRITICAL — reversal failed after NPCI failure. debitReference={}, error={}",
                    debitReference, reversalEx.getMessage(), reversalEx);
            // Persist status = REVERSAL_FAILED / route to manual reconciliation queue here.
            // Consider throwing a 5xx here rather than returning 200, so
            // monitoring/alerting on HTTP error rates catches this too.
            return buildResponse(request, TransactionStatus.NPCI_FAILED,
                    "Disbursal failed and reversal could not be confirmed — escalated for manual review");
        }
    }

    // ASSUMPTION: TransactionResponse is a record with @Builder (like your
    // earlier TransferResponse), exposing an .amount(...) builder method.
    // If TransactionResponse's amount field is also named amountMinorUnits
    // to match the request DTO's naming, rename .amount(...) below to
    // .amountMinorUnits(...) — please confirm against the actual DTO.
    private TransactionResponse buildResponse(TransferRequest request, TransactionStatus status, String failureReason) {
        return TransactionResponse.builder()
                .transactionReference(request.idempotencyKey()) // replace with a real generated transaction ID once persisted
                .status(status)
                .amountMinorUnits(request.amountMinorUnits())
                .failureReason(failureReason)
                .completedAt(Instant.now())
                .build();
    }
}

/*
 * Not included here, since it depends on your persistence layer, but strongly
 * recommended before this goes anywhere near production:
 *
 * 1. Persist a Transaction entity BEFORE step 2 (status=INITIATED) and update
 *    its status at every subsequent step. If the process crashes mid-flow,
 *    you need a durable record to reconcile against, not just an HTTP response.
 * 2. Wrap the idempotencyKey with a unique constraint in the DB so even a
 *    concurrent duplicate request at the API layer (before it reaches CBS)
 *    is rejected.
 * 3. Consider making Steps 2–5 driven by a state machine or a saga
 *    orchestration library (e.g. Axon, or a simple DB-backed status column +
 *    a scheduled reconciliation job) rather than a single synchronous method,
 *    especially once NPCI responses can be asynchronous/webhook-based instead
 *    of a direct synchronous call.
 */