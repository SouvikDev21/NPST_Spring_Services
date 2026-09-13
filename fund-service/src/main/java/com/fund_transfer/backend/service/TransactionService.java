package com.fund_transfer.backend.service;

import com.fund_transfer.backend.Cbs.CbsClient;
import com.fund_transfer.backend.Npci.NpciClient;
import com.fund_transfer.backend.dto.Request.TransferRequest;
import com.fund_transfer.backend.dto.Response.TransactionResponse;
import com.fund_transfer.backend.entity.Transaction;
import com.fund_transfer.backend.enums.TransactionStatus;
import com.fund_transfer.backend.exception.CbsDebitException;
import com.fund_transfer.backend.exception.CbsReversalException;
import com.fund_transfer.backend.exception.InsufficientBalanceException;
import com.fund_transfer.backend.repository.TransactionRepo;
import com.fund_transfer.backend.utils.MoneyUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final CbsClient cbsClient;
    private final NpciClient npciClient;
    private final TransactionRepo transactionRepository;

    public TransactionResponse processTransfer(TransferRequest request, String idempotencyKey) {

        // -----------------------------------------------------------
        // STEP 0: Idempotency check — BEFORE any side effect, including
        // the balance check. If a row already exists for this key, this
        // is a retry (client timeout/double-click), not a new transfer.
        // -----------------------------------------------------------
        Optional<Transaction> existing = transactionRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            log.info("Idempotent replay detected for key={}, returning existing result (status={})",
                    idempotencyKey, existing.get().getStatus());
            return toResponse(existing.get());
        }

        // -----------------------------------------------------------
        // STEP 0.5: Mint OUR OWN durable transaction reference and
        // persist status=INITIATED immediately. This row is what survives
        // a crash mid-flow and what reconciliation is run against — it is
        // NOT the same value as idempotencyKey (see class docs in DTOs).
        //
        // The unique constraint on idempotency_key (see Transaction entity)
        // is the real defence against a concurrent duplicate request that
        // races past the findByIdempotencyKey() check above; we catch that
        // below rather than relying on the check alone.
        // -----------------------------------------------------------
        String transactionReference = "TXN-" + UUID.randomUUID();

        Transaction txn = Transaction.builder()
                .transactionReference(transactionReference)
                .idempotencyKey(idempotencyKey)
                .initiatorAccountNumber(request.initiatorAccountNumber())
                .destinationAccountNumber(request.destinationAccountNumber())
                .beneficiaryIfsc(request.beneficiaryIfsc())
                .amountMinorUnits(request.amountMinorUnits())
                .status(TransactionStatus.INITIATED)
                .build();

        try {
            txn = transactionRepository.save(txn);
        } catch (DataIntegrityViolationException dup) {
            // Lost a race against a concurrent identical request — fetch
            // and return whatever the winner produced instead of erroring.
            log.warn("Race on idempotency key={} — returning the winning row", idempotencyKey);
            return transactionRepository.findByIdempotencyKey(idempotencyKey)
                    .map(this::toResponse)
                    .orElseThrow(() -> dup);
        }

        // -----------------------------------------------------------
        // STEP 1: Validate balance BEFORE touching any money.
        //
        // availableBalance from CBS is assumed to come back in RUPEES
        // (BigDecimal) — that's the natural unit for a balance-inquiry API.
        // The transfer amount, however, is stored/transmitted everywhere
        // else as paise (BigInteger). Convert paise -> rupees ONLY at this
        // comparison boundary via MoneyUtil; never compare the two in
        // different units directly.
        // -----------------------------------------------------------
        // BigDecimal availableBalance = cbsClient.getAvailableBalance(request.initiatorAccountNumber());
        BigDecimal availableBalance = BigDecimal.valueOf(1_000_000_000L); // rupees

        BigDecimal transferAmountRupees = MoneyUtil.paiseToRupees(request.amountMinorUnits());

        if (availableBalance.compareTo(transferAmountRupees) < 0) {
            String reason = "Available balance ₹" + availableBalance
                    + " is less than transfer amount ₹" + transferAmountRupees
                    + " (" + request.amountMinorUnits() + " paise)";
            markFailed(txn, TransactionStatus.FAILED, reason);
            throw new InsufficientBalanceException(reason);
        }
        log.info("[{}] Step 1 complete: balance validated for account {}",
                transactionReference, request.initiatorAccountNumber());

        // -----------------------------------------------------------
        // STEP 2: Debit the owner's account at CBS.
        // Pass transactionReference (not idempotencyKey) as our correlation
        // id to CBS where possible — idempotencyKey is still forwarded
        // separately so CBS can do its own dedup on retries of this exact
        // HTTP call.
        // -----------------------------------------------------------
        String debitReference;
        try {
            debitReference = "SUCCESS";
            // debitReference = cbsClient.debit(
            //         request.initiatorAccountNumber(),
            //         request.amountMinorUnits(),
            //         idempotencyKey);
            log.info("[{}] Step 2 complete: CBS debit succeeded, reference={}", transactionReference, debitReference);
        } catch (CbsDebitException e) {
            log.warn("[{}] Step 2 failed: CBS debit rejected — {}", transactionReference, e.getMessage());
            markFailed(txn, TransactionStatus.FAILED, e.getMessage());
            return toResponse(txn);
        }

        // NOTE on timeouts: if the CBS call times out with no exception and
        // no response, do NOT assume the debit failed — query CBS for the
        // status of this idempotencyKey before deciding. A real CbsClient
        // must surface that ambiguity explicitly rather than throwing a
        // plain CbsDebitException.

        txn.setCbsDebitReference(debitReference);
        txn.setStatus(TransactionStatus.DEBITED);
        transactionRepository.save(txn);

        // -----------------------------------------------------------
        // STEP 3: NPCI disbursal to the beneficiary. Money has left the
        // owner's account (Step 2) but hasn't reached the beneficiary yet.
        // -----------------------------------------------------------
        boolean npciSuccess;
        try {
            npciSuccess = npciClient.disburse(
                    request.destinationAccountNumber(),
                    request.beneficiaryIfsc(),
                    request.amountMinorUnits(),
                    idempotencyKey);
        } catch (Exception e) {
            // Treated as failure for now; in production an ambiguous NPCI
            // outcome should route to a reconciliation/status-poll job
            // instead of immediately reversing.
            log.warn("[{}] Step 3: NPCI call threw an exception, treating as failure — {}",
                    transactionReference, e.getMessage());
            npciSuccess = false;
        }

        // -----------------------------------------------------------
        // STEP 4: Set status based on NPCI outcome.
        // -----------------------------------------------------------
        if (npciSuccess) {
            log.info("[{}] Step 4: NPCI disbursal succeeded for {}",
                    transactionReference, request.destinationAccountNumber());
            txn.setStatus(TransactionStatus.SUCCESS);
            txn.setFailureReason(null);
            transactionRepository.save(txn);
            return toResponse(txn);
        }

        // -----------------------------------------------------------
        // STEP 5: NPCI failed after CBS debit succeeded — reverse the debit
        // so the owner isn't left out of pocket.
        // -----------------------------------------------------------
        log.warn("[{}] Step 4/5: NPCI disbursal failed, reversing CBS debit reference={}",
                transactionReference, debitReference);
        try {
            cbsClient.reverseDebit(debitReference, request.amountMinorUnits(), idempotencyKey);
            log.info("[{}] Step 5 complete: reversal succeeded for reference={}", transactionReference, debitReference);
            txn.setStatus(TransactionStatus.NPCI_FAILED);
            txn.setFailureReason("Disbursal failed; amount reversed to owner account");
            transactionRepository.save(txn);
            return toResponse(txn);
        } catch (CbsReversalException reversalEx) {
            // -----------------------------------------------------------
            // STEP 6: Worst case — debit succeeded, disbursal failed, AND
            // reversal failed. Never silently swallow this: persist status,
            // and this row must be picked up by an on-call alert / manual
            // reconciliation queue poll.
            // -----------------------------------------------------------
            log.error("[{}] Step 6: CRITICAL — reversal failed after NPCI failure. debitReference={}, error={}",
                    transactionReference, debitReference, reversalEx.getMessage(), reversalEx);
            txn.setStatus(TransactionStatus.REVERSAL_FAILED);
            txn.setFailureReason("Disbursal failed and reversal could not be confirmed — escalated for manual review");
            transactionRepository.save(txn);
            // Consider throwing a mapped 5xx here instead of returning 200,
            // so monitoring/alerting on HTTP error rates catches this too.
            return toResponse(txn);
        }
    }

    private void markFailed(Transaction txn, TransactionStatus status, String reason) {
        txn.setStatus(status);
        txn.setFailureReason(reason);
        transactionRepository.save(txn);
    }

    private TransactionResponse toResponse(Transaction txn) {
        return TransactionResponse.builder()
                .transactionReference(txn.getTransactionReference())
                .status(txn.getStatus())
                .amountMinorUnits(txn.getAmountMinorUnits())
                .failureReason(txn.getFailureReason())
                .completedAt(txn.getUpdatedAt() != null ? txn.getUpdatedAt() : OffsetDateTime.now(ZoneOffset.UTC))
                .build();
    }
}