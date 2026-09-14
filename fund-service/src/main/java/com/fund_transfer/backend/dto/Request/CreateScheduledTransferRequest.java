package com.fund_transfer.backend.dto.Request;

import com.fund_transfer.backend.enums.ScheduleFrequency;
import com.fund_transfer.backend.enums.TransferMode;
import jakarta.validation.constraints.*;
import java.math.BigInteger;
import java.time.LocalDate;

public record CreateScheduledTransferRequest(

        @NotBlank(message = "initiatorAccountNumber is required")
        String initiatorAccountNumber,

        @NotNull
        Long beneficiaryId,

        @NotNull
        @Positive
        BigInteger amountMinorUnits,

        @NotNull
        TransferMode transferMode,

        @NotNull
        ScheduleFrequency frequency,

        @NotNull
        @FutureOrPresent
        LocalDate nextExecutionDate,

        LocalDate endDate

) {
    @AssertTrue(message = "endDate must be null or on/after nextExecutionDate")
    public boolean isEndDateValid() {
        return endDate == null || !endDate.isBefore(nextExecutionDate);
    }
}