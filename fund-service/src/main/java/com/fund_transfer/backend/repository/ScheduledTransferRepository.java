package com.fund_transfer.backend.repository;

import com.fund_transfer.backend.entity.ScheduledTransfer;
import com.fund_transfer.backend.enums.ScheduleStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ScheduledTransferRepository
        extends JpaRepository<ScheduledTransfer, Long> {

    List<ScheduledTransfer>
    findByCifOrderByNextExecutionDateAsc(String cif);

    Optional<ScheduledTransfer>
    findByIdAndCif(Long id, String cif);

    List<ScheduledTransfer>
    findByCifAndStatusOrderByNextExecutionDateAsc(
            String cif,
            ScheduleStatus status
    );

    List<ScheduledTransfer>
    findByStatusAndNextExecutionDateLessThanEqual(
            ScheduleStatus status,
            LocalDate date
    );
}