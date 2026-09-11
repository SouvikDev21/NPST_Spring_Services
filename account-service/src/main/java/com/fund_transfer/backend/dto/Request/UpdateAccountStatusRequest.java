package com.fund_transfer.backend.dto.Request;

import com.fund_transfer.backend.enums.AccountStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAccountStatusRequest {

    @NotNull(message = "New account status is required")
    private AccountStatus status;

    private String reason;
}