package com.term_deposit.backend.service;

import com.term_deposit.backend.entity.InterestAccrualLog;
import com.term_deposit.backend.entity.TermDeposit;
import com.term_deposit.backend.entity.TermDepositTaxRecord;
import com.term_deposit.backend.enums.DepositStatus;
import com.term_deposit.backend.enums.TdsStatus;
import com.term_deposit.backend.enums.TaxRecordStatus;
import com.term_deposit.backend.repository.InterestAccrualLogRepository;
import com.term_deposit.backend.repository.TermDepositRepository;
import com.term_deposit.backend.repository.TermDepositTaxRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EodBatchService {

    private final TermDepositRepository termDepositRepository;
    private final InterestAccrualLogRepository accrualLogRepository;
    private final TermDepositTaxRecordRepository taxRecordRepository;

    @Scheduled(cron = "*/10 * * * * *")
    @Transactional
    public void executeDailyInterestAccrual() {
        log.info("--- POLLING: Checking for active/new Term Deposits for Interest Accrual ---");

        List<TermDeposit> activeDeposits = termDepositRepository.findByStatus(DepositStatus.ACTIVE);

        if (activeDeposits.isEmpty()) {
            return;
        }

        LocalDate today = LocalDate.now();
        int processedCount = 0;

        for (TermDeposit fd : activeDeposits) {
            try {
                LocalDate accrualDate = today;
                if (fd.getActivatedAt() != null) {
                    accrualDate = today;
                }

                // ==========================================
                // 1. INTEREST LOGIC (Protected by Idempotency)
                // ==========================================
                boolean alreadyAccrued = accrualLogRepository.existsByTermDepositIdAndAccrualDate(fd.getId(), accrualDate);

                if (!alreadyAccrued) {
                    BigDecimal dailyInterest = calculateDailyInterest(fd.getPrincipalAmount(), fd.getInterestRate());

                    InterestAccrualLog logEntry = new InterestAccrualLog();
                    logEntry.setTermDepositId(fd.getId());
                    logEntry.setAccrualDate(accrualDate);
                    logEntry.setAccruedAmount(dailyInterest);
                    accrualLogRepository.save(logEntry);

                    BigInteger dailyInterestMinorUnits = dailyInterest.multiply(new BigDecimal("100")).toBigInteger();
                    if (fd.getInterestEarnedMinorUnits() == null) {
                        fd.setInterestEarnedMinorUnits(BigInteger.ZERO);
                    }
                    fd.setInterestEarnedMinorUnits(fd.getInterestEarnedMinorUnits().add(dailyInterestMinorUnits));
                    termDepositRepository.save(fd);

                    log.info("Successfully accrued daily interest for FD ID: {}", fd.getId());
                    processedCount++;
                }

                // ==========================================
                // 2. MONTHLY TAX LOGIC (Independent of Interest Loop)
                // ==========================================
                boolean isEndOfMonth = true; // 💡 FOR TESTING: Forced to true

                // Only calculate tax if principal is greater than 0
                if (isEndOfMonth && fd.getPrincipalAmount().compareTo(BigDecimal.ZERO) > 0) {
                    boolean taxProcessed = processMonthlyTdsDeduction(fd, today);
                    if (taxProcessed) {
                        processedCount++;
                    }
                }

            } catch (Exception e) {
                log.error("Failed to process batch for FD ID: {}. Error: {}", fd.getId(), e.getMessage());
            }
        }

        if (processedCount > 0) {
            log.info("--- BATCH COMPLETE: Processed updates for {} account(s) ---", processedCount);
        }
    }

    private boolean processMonthlyTdsDeduction(TermDeposit fd, LocalDate today) {
        String financialYear = calculateFinancialYear(today);

        // Fetch existing record if it exists
        TermDepositTaxRecord existingRecord = taxRecordRepository
                .findByDepositIdAndFinancialYear(fd.getId(), financialYear)
                .orElse(null);

        // 🛡️ TAX IDEMPOTENCY GUARD: Prevents infinite adding during 10-second polling tests
        if (existingRecord != null && existingRecord.getTdsStatus() == TdsStatus.DEDUCTED) {
            return false; // Skip, already deducted this cycle!
        }

        BigDecimal monthlyInterestDecimal = calculateDailyInterest(fd.getPrincipalAmount(), fd.getInterestRate()).multiply(new BigDecimal(today.lengthOfMonth()));
        BigInteger monthlyInterestMinorUnits = monthlyInterestDecimal.multiply(new BigDecimal("100")).toBigInteger();

        if (monthlyInterestMinorUnits.compareTo(BigInteger.ZERO) <= 0) {
            return false;
        }

        BigInteger calculatedTaxMinorUnits = new BigDecimal(monthlyInterestMinorUnits)
                .multiply(new BigDecimal("0.10"))
                .setScale(0, RoundingMode.HALF_UP)
                .toBigInteger();

        BigInteger finalTaxMinorUnits = calculatedTaxMinorUnits.min(monthlyInterestMinorUnits);

        TermDepositTaxRecord taxRecord = (existingRecord != null) ? existingRecord : TermDepositTaxRecord.builder()
                .depositId(fd.getId())
                .cif(fd.getCif())
                .financialYear(financialYear)
                .bankCode(fd.getBankCode())
                .interestEarnedMinorUnits(BigInteger.ZERO)
                .tdsAmountMinorUnits(BigInteger.ZERO)
                .tdsApplicable(true)
                .tdsStatus(TdsStatus.PENDING)
                .status(TaxRecordStatus.FINAL)
                .build();

        taxRecord.setInterestEarnedMinorUnits(taxRecord.getInterestEarnedMinorUnits().add(monthlyInterestMinorUnits));
        taxRecord.setTdsAmountMinorUnits(taxRecord.getTdsAmountMinorUnits().add(finalTaxMinorUnits));
        taxRecord.setTdsStatus(TdsStatus.DEDUCTED);

        taxRecordRepository.save(taxRecord);
        log.info("Tax successfully deducted and saved for FD ID: {}", fd.getId());
        return true;
    }

    private String calculateFinancialYear(LocalDate date) {
        int year = date.getYear();
        if (date.getMonthValue() < 4) {
            return (year - 1) + "-" + year;
        } else {
            return year + "-" + (year + 1);
        }
    }

    private BigDecimal calculateDailyInterest(BigDecimal principal, BigDecimal annualInterestRate) {
        BigDecimal divisor = new BigDecimal("36500");
        BigDecimal dailyRate = annualInterestRate.divide(divisor, 8, RoundingMode.HALF_UP);
        return principal.multiply(dailyRate).setScale(2, RoundingMode.HALF_UP);
    }
}