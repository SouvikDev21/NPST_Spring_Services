package com.fund_transfer.backend.dto.request.customer;

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
public class CustomerAccountsRequest {
    private String customerId;
    private AccountType accountType;
    private AccountStatus status;
    private Integer page;
    private Integer size;
}
