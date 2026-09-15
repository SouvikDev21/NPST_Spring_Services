package com.term_deposit.backend.dto.Mapper;

import com.term_deposit.backend.entity.TermDeposit;
import com.term_deposit.backend.dto.Response.TermDepositResponse;

public class TermDepositMapper {

    // Translates the Database Entity into a Frontend DTO
    public static TermDepositResponse toResponseDto(TermDeposit entity) {
        return new TermDepositResponse(
                entity.getDepositNumber(),
                entity.getPrincipalMinorUnits(),
                entity.getMaturityDate(),
                entity.getStatus().name()
        );
    }
}