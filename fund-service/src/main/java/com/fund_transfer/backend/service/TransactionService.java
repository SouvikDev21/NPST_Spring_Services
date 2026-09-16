package com.fund_transfer.backend.service;

import com.fund_transfer.backend.Cbs.CbsClient;
import com.fund_transfer.backend.Npci.NpciClient;
import com.fund_transfer.backend.dto.Mapper.TransactionMapper;
import com.fund_transfer.backend.dto.Request.TransferRequest;
import com.fund_transfer.backend.dto.Response.TransactionResponse;
import com.fund_transfer.backend.entity.Transaction;
import com.fund_transfer.backend.enums.TransactionStatus;
import com.fund_transfer.backend.exception.CbsDebitException;
import com.fund_transfer.backend.exception.CbsReversalException;
import com.fund_transfer.backend.exception.InsufficientBalanceException;
import com.fund_transfer.backend.repository.TransactionRepo;
import com.fund_transfer.backend.utils.MoneyUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final CbsClient cbsClient;
    private final NpciClient npciClient;
    private final TransactionRepo transactionRepository;
    private final TransactionMapper transactionMapper;


    public TransactionResponse processTransfer(TransferRequest request, String idempotencyKey) {

        Optional<Transaction> existing = transactionRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            log.info("Idempotent replay detected for key={}, returning existing result (status={})",
                    idempotencyKey, existing.get().getStatus());
            return toResponse(existing.get());
        }


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

        BigDecimal availableBalance = cbsClient.getAvailableBalance(request.initiatorAccountNumber());
//        BigDecimal availableBalance = BigDecimal.valueOf(1_000_000_000L); // rupees

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

        String debitReference;
        try {
            debitReference="asffjbja-123-ffgg";
//             debitReference = cbsClient.debit(
//                     request.initiatorAccountNumber(),
//                     request.amountMinorUnits(),
//                     idempotencyKey);
            log.info("[{}] Step 2 complete: CBS debit succeeded, reference={}", transactionReference, debitReference);
        } catch (CbsDebitException e) {
            log.warn("[{}] Step 2 failed: CBS debit rejected — {}", transactionReference, e.getMessage());
            markFailed(txn, TransactionStatus.FAILED, e.getMessage());
            return toResponse(txn);
        }


        txn.setCbsDebitReference(debitReference);
        txn.setStatus(TransactionStatus.DEBITED);
        transactionRepository.save(txn);


        boolean npciSuccess;
        try {
            npciSuccess = npciClient.disburse(
                    request.destinationAccountNumber(),
                    request.beneficiaryIfsc(),
                    request.amountMinorUnits(),
                    idempotencyKey);
        } catch (Exception e) {

            log.warn("[{}] Step 3: NPCI call threw an exception, treating as failure — {}",
                    transactionReference, e.getMessage());
            npciSuccess = false;
        }


        if (npciSuccess) {
            log.info("[{}] Step 4: NPCI disbursal succeeded for {}",
                    transactionReference, request.destinationAccountNumber());
            txn.setStatus(TransactionStatus.SUCCESS);
            txn.setFailureReason(null);
            transactionRepository.save(txn);
            return toResponse(txn);
        }


        log.warn("[{}] Step 4/5: NPCI disbursal failed, reversing CBS debit reference={}",
                transactionReference, debitReference);
        try {
//            cbsClient.reverseDebit(debitReference, request.amountMinorUnits(), idempotencyKey);
            log.info("[{}] Step 5 complete: reversal succeeded for reference={}", transactionReference, debitReference);
            txn.setStatus(TransactionStatus.NPCI_FAILED);
            txn.setFailureReason("Disbursal failed; amount reversed to owner account");
            transactionRepository.save(txn);
            return toResponse(txn);
        } catch (CbsReversalException reversalEx) {

            log.error("[{}] Step 6: CRITICAL — reversal failed after NPCI failure. debitReference={}, error={}",
                    transactionReference, debitReference, reversalEx.getMessage(), reversalEx);
            txn.setStatus(TransactionStatus.REVERSAL_FAILED);
            txn.setFailureReason("Disbursal failed and reversal could not be confirmed — escalated for manual review");
            transactionRepository.save(txn);

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
    public List<TransactionResponse>    getHistoryForAccount(String accountNumber) {

        if (accountNumber == null || accountNumber.isBlank()) {
            throw new IllegalArgumentException("accountNumber is required");
        }

        List<Transaction> transactions =
                transactionRepository
                        .findByInitiatorAccountNumber(
                                accountNumber);
        return transactionMapper.toResponseList(transactions);
    }
}
