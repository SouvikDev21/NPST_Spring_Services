package com.fund_transfer.backend.dto.request.statement;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fund_transfer.backend.common.constants.AppConstants;
import com.fund_transfer.backend.enums.StatementFormat;
import jakarta.validation.constraints.Email;
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
public class StatementEmailRequest {

    @NotBlank(message = "Account number is required")
    private String accountNumber;

    @NotBlank(message = "Email address is required")
    @Email(message = "Valid email address is required")
    private String emailAddress;

    @JsonFormat(pattern = AppConstants.ISO_DATE_FORMAT)
    private LocalDate fromDate;

    @JsonFormat(pattern = AppConstants.ISO_DATE_FORMAT)
    private LocalDate toDate;

    private StatementFormat format;
    private Boolean passwordProtected;
    private String password;
}
