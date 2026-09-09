package com.fund_transfer.backend.dto.request.statement;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fund_transfer.backend.common.constants.AppConstants;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatementSearchRequest {

    @NotBlank(message = "Account number is required")
    private String accountNumber;

    @JsonFormat(pattern = AppConstants.ISO_DATE_FORMAT)
    private LocalDate fromDate;

    @JsonFormat(pattern = AppConstants.ISO_DATE_FORMAT)
    private LocalDate toDate;

    private Integer page;
    private Integer size;
}
