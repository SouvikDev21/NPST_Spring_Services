package com.fund_transfer.backend.dto.Mapper;

import com.fund_transfer.backend.dto.Response.BeneficiaryResponse;
import com.fund_transfer.backend.entity.Beneficiary;
import org.springframework.stereotype.Component;

@Component
public class BeneficiaryMapper {

    public BeneficiaryResponse toResponse(Beneficiary beneficiary) {
        if (beneficiary == null) {
            return null;
        }

        return new BeneficiaryResponse(
                beneficiary.getId(),
                beneficiary.getOwnerCif(),
                beneficiary.getBeneficiaryName(),
                beneficiary.getBeneficiaryAccountNumber(),
                beneficiary.getBeneficiaryIfscCode(),
                beneficiary.getBeneficiaryBankName(),
                beneficiary.getNickname(),
                beneficiary.getTransferMode(),
                beneficiary.getStatus(),
                beneficiary.getType(),
                beneficiary.getCoolingPeriodEndsAt(),
                beneficiary.getDailyLimitMinorUnits()
        );
    }
}
