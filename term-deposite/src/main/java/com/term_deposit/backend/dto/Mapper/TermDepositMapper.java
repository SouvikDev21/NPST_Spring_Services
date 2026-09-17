package com.term_deposit.backend.dto.Mapper;

import com.term_deposit.backend.dto.Request.OpenDepositRequest;
import com.term_deposit.backend.dto.Response.TermDepositResponse;
import com.term_deposit.backend.entity.DepositRequest;
import com.term_deposit.backend.entity.TermDeposit;

public class TermDepositMapper {

    // 1. Translates the Frontend DTO into a Database Entity (Incoming)
    public static DepositRequest toEntity(OpenDepositRequest dto) {
        DepositRequest depositRequest = new DepositRequest();

        depositRequest.setCif(dto.cif());
        depositRequest.setMakerKeycloakUserId(dto.makerUserId());
        // Add your new dynamic fields here! (Notice there is no "get" because it is a record)
        depositRequest.setPrincipalAmount(dto.principalAmount());
        depositRequest.setTenureMonths(dto.tenureMonths());

        return depositRequest;
    }

    // 2. Translates the Database Entity into a Frontend DTO (Outgoing)
    public static TermDepositResponse toResponseDto(TermDeposit entity) {
        return new TermDepositResponse(
                entity.getDepositNumber(),
                entity.getPrincipalMinorUnits(),
                entity.getMaturityDate(),
                entity.getStatus().name()
        );
    }
}