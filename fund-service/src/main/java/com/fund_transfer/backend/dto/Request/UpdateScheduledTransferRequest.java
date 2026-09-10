package com.fund_transfer.backend.dto.Request;

import com.fund_transfer.backend.enums.ScheduleFrequency;
import com.fund_transfer.backend.enums.TransferMode;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigInteger;
import java.time.LocalDate;

public record UpdateScheduledTransferRequest(

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
}