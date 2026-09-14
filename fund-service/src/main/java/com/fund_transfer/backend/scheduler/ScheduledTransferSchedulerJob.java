package com.fund_transfer.backend.scheduler;

import com.fund_transfer.backend.dto.Request.TransferRequest;
import com.fund_transfer.backend.dto.Response.TransactionResponse;
import com.fund_transfer.backend.entity.Beneficiary;
import com.fund_transfer.backend.entity.ScheduledTransfer;
import com.fund_transfer.backend.enums.ScheduleFrequency;
import com.fund_transfer.backend.enums.ScheduleStatus;
import com.fund_transfer.backend.enums.TransactionStatus;
import com.fund_transfer.backend.repository.ScheduledTransferRepository;
import com.fund_transfer.backend.service.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Component
public class ScheduledTransferSchedulerJob {

    private static final Logger log =
            LoggerFactory.getLogger(ScheduledTransferSchedulerJob.class);

    private final ScheduledTransferRepository scheduledTransferRepository;
    private final ScheduledTransferBeneficiaryResolver beneficiaryResolver;
    private final TransactionService transactionService;

    public ScheduledTransferSchedulerJob(
            ScheduledTransferRepository scheduledTransferRepository,
            ScheduledTransferBeneficiaryResolver beneficiaryResolver,
            TransactionService transactionService) {

        this.scheduledTransferRepository = scheduledTransferRepository;
        this.beneficiaryResolver = beneficiaryResolver;
        this.transactionService = transactionService;
    }

    /**
     * Runs every day at 12:00 AM IST.
     */
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Kolkata")
    public void processScheduledTransfers() {

        LocalDate today = LocalDate.now();

        log.info("Scheduled transfer job started for date: {}", today);

        List<ScheduledTransfer> dueTransfers =
                scheduledTransferRepository
                        .findByStatusAndNextExecutionDateLessThanEqual(
                                ScheduleStatus.ACTIVE, today);

        if (dueTransfers.isEmpty()) {
            log.info("No scheduled transfers are due for execution.");
            return;
        }

        log.info("Found {} scheduled transfer(s) due for execution.", dueTransfers.size());

        for (ScheduledTransfer scheduledTransfer : dueTransfers) {
            // Each schedule is its own transaction so one failure doesn't
            // roll back the others in the same batch.
            processTransfer(scheduledTransfer);
        }
    }

    @Transactional
    void processTransfer(ScheduledTransfer scheduledTransfer) {

        log.info(
                "Processing scheduled transfer. id={}, cif={}, beneficiaryId={}, amount={}",
                scheduledTransfer.getId(),
                scheduledTransfer.getCif(),
                scheduledTransfer.getBeneficiaryId(),
                scheduledTransfer.getAmountMinorUnits()
        );

        // ---- Resolve beneficiary account/IFSC ----
        Beneficiary beneficiary;
        try {
            beneficiary = beneficiaryResolver.resolve(scheduledTransfer);
        } catch (IllegalStateException e) {
            log.warn("[{}] Beneficiary resolution failed: {}",
                    scheduledTransfer.getId(), e.getMessage());
            recordFailedAttempt(scheduledTransfer, e.getMessage());
            scheduledTransferRepository.save(scheduledTransfer);
            return;
        }

        TransferRequest transferRequest = new TransferRequest(
                scheduledTransfer.getInitiatorAccountNumber(),
                beneficiary.getBeneficiaryAccountNumber(),
                beneficiary.getBeneficiaryIfscCode(),
                scheduledTransfer.getAmountMinorUnits()
        );

        // One key per (schedule, due-date) — a retry on the SAME due date
        // (e.g. job re-run after a crash) replays instead of double-debiting.
        // A retry on the NEXT due date gets a fresh key, as it should.
        String idempotencyKey =
                "SCHED-" + scheduledTransfer.getId() + "-" + scheduledTransfer.getNextExecutionDate();

        // ---- Call TransactionService ----
        TransactionResponse response;
        try {
            response = transactionService.processTransfer(transferRequest, idempotencyKey);
        } catch (Exception ex) {
            log.error("[{}] TransactionService threw an exception: {}",
                    scheduledTransfer.getId(), ex.getMessage(), ex);
            recordFailedAttempt(scheduledTransfer, ex.getMessage());
            scheduledTransferRepository.save(scheduledTransfer);
            return;
        }

        scheduledTransfer.setLastExecutedAt(Instant.now());

        switch (response.status()) {

            case SUCCESS -> {
                log.info("[{}] Execution succeeded. txnRef={}",
                        scheduledTransfer.getId(), response.transactionReference());
                scheduledTransfer.setLastExecutionStatus(ScheduleStatus.ACTIVE);
                scheduledTransfer.setRetryCount(0L);
                advanceOrComplete(scheduledTransfer);
            }

            case REVERSAL_FAILED -> {
                // Worst case from TransactionService: debit succeeded, disbursal
                // failed, AND reversal failed. Do NOT auto-retry — that could
                // debit the owner again while the first debit is still stuck.
                // Stop the schedule and force manual review.
                log.error("[{}] CRITICAL: txnRef={} is REVERSAL_FAILED — stopping schedule, needs manual reconciliation",
                        scheduledTransfer.getId(), response.transactionReference());
                scheduledTransfer.setLastExecutionStatus(ScheduleStatus.FAILED);
                scheduledTransfer.setStatus(ScheduleStatus.FAILED);
            }

            default -> {
                // FAILED / NPCI_FAILED / DEBITED / INITIATED — no successful
                // disbursal. Owner's money is safe (or already refunded), so
                // this is retry-eligible on the next job run.
                log.warn("[{}] Execution did not succeed. status={}, reason={}",
                        scheduledTransfer.getId(), response.status(), response.failureReason());
                recordFailedAttempt(scheduledTransfer, response.failureReason());
            }
        }

        scheduledTransferRepository.save(scheduledTransfer);
    }

    /**
     * Moves an ONE_TIME schedule to COMPLETED after its single successful run,
     * or advances a recurring schedule's nextExecutionDate — completing it
     * instead if the new date would fall after endDate.
     */
    private void advanceOrComplete(ScheduledTransfer scheduledTransfer) {

        if (scheduledTransfer.getFrequency() == ScheduleFrequency.ONE_TIME) {
            scheduledTransfer.setStatus(ScheduleStatus.COMPLETED);
            return;
        }

        LocalDate next = nextDateFor(
                scheduledTransfer.getFrequency(),
                scheduledTransfer.getNextExecutionDate());

        LocalDate endDate = scheduledTransfer.getEndDate();
        if (endDate != null && next.isAfter(endDate)) {
            scheduledTransfer.setStatus(ScheduleStatus.COMPLETED);
            return;
        }

        scheduledTransfer.setNextExecutionDate(next);
    }

    private LocalDate nextDateFor(ScheduleFrequency frequency, LocalDate from) {
        return switch (frequency) {
            case DAILY -> from.plusDays(1);
            case WEEKLY -> from.plusWeeks(1);
            case MONTHLY -> from.plusMonths(1);
            case ONE_TIME -> from; // unreachable — handled in advanceOrComplete
        };
    }

    private void recordFailedAttempt(ScheduledTransfer scheduledTransfer, String reason) {
        long retries = scheduledTransfer.getRetryCount() + 1;
        scheduledTransfer.setRetryCount(retries);
        scheduledTransfer.setLastExecutionStatus(ScheduleStatus.FAILED);

        if (retries >= scheduledTransfer.getMaxRetries()) {
            log.error("[{}] Max retries ({}) reached — marking schedule FAILED. Last reason: {}",
                    scheduledTransfer.getId(), scheduledTransfer.getMaxRetries(), reason);
            scheduledTransfer.setStatus(ScheduleStatus.FAILED);
        }
    }
}