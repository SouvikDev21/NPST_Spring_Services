package com.fund_transfer.backend.dto.Mapper;

import com.fund_transfer.backend.dto.Response.ScheduledTransferResponse;
import com.fund_transfer.backend.entity.ScheduledTransfer;
import org.springframework.stereotype.Component;

@Component
public class ScheduledTransferMapper {

    public ScheduledTransferResponse toResponse(
            ScheduledTransfer scheduledTransfer) {

        if (scheduledTransfer == null) {
            return null;
        }

        return new ScheduledTransferResponse(
                scheduledTransfer.getId(),
                scheduledTransfer.getCif(),
                scheduledTransfer.getKeycloakUserId(),
                scheduledTransfer.getBeneficiaryId(),
                scheduledTransfer.getAmountMinorUnits(),
                scheduledTransfer.getTransferMode(),
                scheduledTransfer.getFrequency(),
                scheduledTransfer.getNextExecutionDate(),
                scheduledTransfer.getEndDate(),
                scheduledTransfer.getStatus(),
                scheduledTransfer.getLastExecutionStatus(),
                scheduledTransfer.getLastExecutedAt(),
                scheduledTransfer.getRetryCount(),
                scheduledTransfer.getMaxRetries(),
                scheduledTransfer.getVersion(),
                scheduledTransfer.getCreatedAt(),
                scheduledTransfer.getUpdatedAt()
        );
    }
}