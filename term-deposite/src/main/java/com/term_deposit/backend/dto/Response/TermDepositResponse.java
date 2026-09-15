package com.term_deposit.backend.dto.Response;

import java.math.BigInteger;
import java.time.LocalDate;

public record TermDepositResponse(
        String depositNumber,
        BigInteger principalMinorUnits,
        LocalDate maturityDate,
        String status
) {}