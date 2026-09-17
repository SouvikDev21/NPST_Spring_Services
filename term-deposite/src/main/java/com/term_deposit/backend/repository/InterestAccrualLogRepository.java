package com.term_deposit.backend.repository;

import com.term_deposit.backend.entity.InterestAccrualLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.UUID;

@Repository
public interface InterestAccrualLogRepository extends JpaRepository<InterestAccrualLog, Long> {

    // THIS IS THE IDEMPOTENCY GUARD METHOD:
    boolean existsByTermDepositIdAndAccrualDate(UUID termDepositId, LocalDate accrualDate);
}