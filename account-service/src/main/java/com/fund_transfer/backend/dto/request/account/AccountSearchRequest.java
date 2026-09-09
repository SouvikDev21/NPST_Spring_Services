package com.fund_transfer.backend.dto.request.account;

import com.fund_transfer.backend.enums.AccountStatus;
import com.fund_transfer.backend.enums.AccountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountSearchRequest {
    private String customerId;
    private String accountNumber;
    private AccountType accountType;
    private AccountStatus status;
    private Integer page;
    private Integer size;
}
