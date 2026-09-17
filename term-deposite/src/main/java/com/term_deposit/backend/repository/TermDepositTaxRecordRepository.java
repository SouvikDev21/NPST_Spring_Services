package com.term_deposit.backend.repository;

import com.term_deposit.backend.entity.TermDepositTaxRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TermDepositTaxRecordRepository extends JpaRepository<TermDepositTaxRecord, Long> {

    // Find existing tax record for a deposit in a specific financial year to update it cumulatively
    Optional<TermDepositTaxRecord> findByDepositIdAndFinancialYear(UUID depositId, String financialYear);
}