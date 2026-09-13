package com.fund_transfer.backend.scheduler;

import com.fund_transfer.backend.entity.ScheduledTransfer;
import com.fund_transfer.backend.enums.ScheduleStatus;
import com.fund_transfer.backend.repository.ScheduledTransferRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class ScheduledTransferSchedulerJob {

    private static final Logger log =
            LoggerFactory.getLogger(ScheduledTransferSchedulerJob.class);

    private final ScheduledTransferRepository scheduledTransferRepository;

    public ScheduledTransferSchedulerJob(
            ScheduledTransferRepository scheduledTransferRepository) {

        this.scheduledTransferRepository =
                scheduledTransferRepository;
    }

    /**
     * Runs every day at 12:00 AM IST.
     */
    @Scheduled(
            cron = "0 0 0 * * *",
            zone = "Asia/Kolkata"
    )
    public void processScheduledTransfers() {

        LocalDate today = LocalDate.now();

        log.info(
                "Scheduled transfer job started for date: {}",
                today
        );

        List<ScheduledTransfer> dueTransfers =
                scheduledTransferRepository
                        .findByStatusAndNextExecutionDateLessThanEqual(
                                ScheduleStatus.ACTIVE,
                                today
                        );

        if (dueTransfers.isEmpty()) {

            log.info(
                    "No scheduled transfers are due for execution."
            );

            return;
        }

        log.info(
                "Found {} scheduled transfer(s) due for execution.",
                dueTransfers.size()
        );

        for (ScheduledTransfer scheduledTransfer : dueTransfers) {

            processTransfer(scheduledTransfer);
        }
    }

    private void processTransfer(
            ScheduledTransfer scheduledTransfer) {

        log.info(
                "Processing scheduled transfer. id={}, cif={}, beneficiaryId={}, amount={}",
                scheduledTransfer.getId(),
                scheduledTransfer.getCif(),
                scheduledTransfer.getBeneficiaryId(),
                scheduledTransfer.getAmountMinorUnits()
        );

        /*
         * Actual IMPS transfer execution will be added here.
         *
         * Future flow:
         *
         * ScheduledTransfer
         *        ↓
         * Validate beneficiary
         *        ↓
         * FundTransferService
         *        ↓
         * IMPS
         *        ↓
         * NPST Switch
         *        ↓
         * Transaction
         *
         * Do NOT mark the schedule as successfully executed
         * until the actual transfer succeeds.
         */
    }
}