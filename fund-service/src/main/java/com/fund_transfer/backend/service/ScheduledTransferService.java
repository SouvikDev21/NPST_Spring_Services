package com.fund_transfer.backend.service;

import com.fund_transfer.backend.dto.Mapper.ScheduledTransferMapper;
import com.fund_transfer.backend.dto.Request.CreateScheduledTransferRequest;
import com.fund_transfer.backend.dto.Request.UpdateScheduledTransferRequest;
import com.fund_transfer.backend.dto.Response.ScheduledTransferResponse;
import com.fund_transfer.backend.entity.ScheduledTransfer;
import com.fund_transfer.backend.enums.ScheduleStatus;
import com.fund_transfer.backend.repository.ScheduledTransferRepository;
import com.fund_transfer.backend.repository.ScheduledTransferRepository;
import com.fund_transfer.backend.security.AuthenticatedUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ScheduledTransferService {

    private final ScheduledTransferRepository scheduledTransferRepo;
    private final ScheduledTransferMapper scheduledTransferMapper;

    public ScheduledTransferService(
            ScheduledTransferRepository scheduledTransferRepo,
            ScheduledTransferMapper scheduledTransferMapper) {

        this.scheduledTransferRepo = scheduledTransferRepo;
        this.scheduledTransferMapper = scheduledTransferMapper;
    }

    @Transactional
    public ScheduledTransferResponse create(
            AuthenticatedUser user,
            CreateScheduledTransferRequest request) {

        validateDates(
                request.nextExecutionDate(),
                request.endDate()
        );

        ScheduledTransfer scheduledTransfer =
                ScheduledTransfer.builder()
                        .cif(user.cif())
                        .keycloakUserId(user.keycloakUserId())
                        .beneficiaryId(request.beneficiaryId())
                        .amountMinorUnits(request.amountMinorUnits())
                        .transferMode(request.transferMode())
                        .frequency(request.frequency())
                        .nextExecutionDate(
                                request.nextExecutionDate())
                        .endDate(request.endDate())
                        .status(ScheduleStatus.ACTIVE)
                        .retryCount(0L)
                        .maxRetries(3L)
                        .build();

        ScheduledTransfer saved =
                scheduledTransferRepo.save(scheduledTransfer);

        return scheduledTransferMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ScheduledTransferResponse> getAll(
            String cif) {

        return scheduledTransferRepo
                .findByCifOrderByNextExecutionDateAsc(cif)
                .stream()
                .map(scheduledTransferMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ScheduledTransferResponse getById(
            Long id,
            String cif) {

        ScheduledTransfer scheduledTransfer =
                findCustomerSchedule(id, cif);

        return scheduledTransferMapper.toResponse(
                scheduledTransfer
        );
    }

    @Transactional
    public ScheduledTransferResponse update(
            Long id,
            String cif,
            UpdateScheduledTransferRequest request) {

        ScheduledTransfer scheduledTransfer =
                findCustomerSchedule(id, cif);

        if (scheduledTransfer.getStatus()
                != ScheduleStatus.ACTIVE) {

            throw new IllegalStateException(
                    "Only ACTIVE scheduled transfers can be updated"
            );
        }

        validateDates(
                request.nextExecutionDate(),
                request.endDate()
        );

        scheduledTransfer.setBeneficiaryId(
                request.beneficiaryId()
        );

        scheduledTransfer.setAmountMinorUnits(
                request.amountMinorUnits()
        );

        scheduledTransfer.setTransferMode(
                request.transferMode()
        );

        scheduledTransfer.setFrequency(
                request.frequency()
        );

        scheduledTransfer.setNextExecutionDate(
                request.nextExecutionDate()
        );

        scheduledTransfer.setEndDate(
                request.endDate()
        );

        ScheduledTransfer updated =
                scheduledTransferRepo.save(scheduledTransfer);

        return scheduledTransferMapper.toResponse(updated);
    }

    @Transactional
    public ScheduledTransferResponse cancel(
            Long id,
            String cif) {

        ScheduledTransfer scheduledTransfer =
                findCustomerSchedule(id, cif);

        if (scheduledTransfer.getStatus()
                == ScheduleStatus.COMPLETED) {

            throw new IllegalStateException(
                    "Completed scheduled transfer cannot be cancelled"
            );
        }

        scheduledTransfer.setStatus(
                ScheduleStatus.CANCELLED
        );

        ScheduledTransfer saved =
                scheduledTransferRepo.save(scheduledTransfer);

        return scheduledTransferMapper.toResponse(saved);
    }

    @Transactional
    public ScheduledTransferResponse changeStatus(
            Long id,
            String cif,
            ScheduleStatus status) {

        ScheduledTransfer scheduledTransfer =
                findCustomerSchedule(id, cif);

        if (status == ScheduleStatus.CANCELLED) {
            throw new IllegalArgumentException(
                    "Use the cancel endpoint to cancel a schedule"
            );
        }

        scheduledTransfer.setStatus(status);

        ScheduledTransfer saved =
                scheduledTransferRepo.save(scheduledTransfer);

        return scheduledTransferMapper.toResponse(saved);
    }

    private ScheduledTransfer findCustomerSchedule(
            Long id,
            String cif) {

        return scheduledTransferRepo
                .findByIdAndCif(id, cif)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Scheduled transfer not found"
                        ));
    }

    private void validateDates(
            LocalDate nextExecutionDate,
            LocalDate endDate) {

        if (endDate != null &&
                endDate.isBefore(nextExecutionDate)) {

            throw new IllegalArgumentException(
                    "End date cannot be before next execution date"
            );
        }
    }
}