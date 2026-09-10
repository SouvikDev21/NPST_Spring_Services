package com.account_service.backend.service;

import com.account_service.backend.dto.account.AccountDto.*;
import com.account_service.backend.enums.AccountStatus;
import com.account_service.backend.enums.AccountType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AccountServiceTest {

    @Autowired
    private AccountService accountService;

    @Test
    void testGetAccountByNumber() {
        AccountResponse response = accountService.getAccountByAccountNumber("101000000001");
        assertNotNull(response);
        assertEquals("101000000001", response.getAccountNumber());
        assertEquals("CIF100001", response.getCustomerId());
    }

    @Test
    void testCreateAccount() {
        CreateRequest request = CreateRequest.builder()
                .customerId("CIF100001")
                .accountType(AccountType.SAVINGS)
                .initialDeposit(new BigDecimal("5000.00"))
                .currency("INR")
                .branchCode("001")
                .build();

        AccountResponse response = accountService.createAccount(request);
        assertNotNull(response);
        assertNotNull(response.getAccountNumber());
        assertEquals(AccountStatus.ACTIVE, response.getStatus());
        assertEquals(new BigDecimal("5000.00"), response.getBalance());
    }

    @Test
    void testGetAccountBalance() {
        BalanceResponse response = accountService.getAccountBalance(BalanceRequest.builder().accountNumber("101000000001").build());
        assertNotNull(response);
        assertEquals("101000000001", response.getAccountNumber());
        assertNotNull(response.getBalance());
    }
}
