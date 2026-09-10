package com.fund_transfer.backend.dto.Request;

import com.fund_transfer.backend.enums.ScheduleFrequency;
import com.fund_transfer.backend.enums.TransferMode;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.FutureOrPresent;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.UUID;

public record CreateScheduledTransferRequest(

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
