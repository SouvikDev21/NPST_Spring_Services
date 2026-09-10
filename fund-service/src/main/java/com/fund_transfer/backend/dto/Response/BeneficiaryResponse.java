package com.fund_transfer.backend.dto.Response;

import com.fund_transfer.backend.enums.BeneficiaryStatus;
import com.fund_transfer.backend.enums.BeneficiaryType;
import com.fund_transfer.backend.enums.TransferMode;

import java.math.BigInteger;
import java.time.Instant;

public record BeneficiaryResponse(
        Long id,
        String ownerCif,
        String beneficiaryName,
        String beneficiaryAccountNumber,
        String beneficiaryIfscCode,
        String beneficiaryBankName,
        String nickname,
        TransferMode transferMode,
        BeneficiaryStatus status,
        BeneficiaryType type,
        Instant coolingPeriodEndsAt,
        BigInteger dailyLimitMinorUnits
) {
}
