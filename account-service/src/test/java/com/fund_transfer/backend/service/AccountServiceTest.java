package com.fund_transfer.backend.service;

import com.fund_transfer.backend.dto.Request.CreateAccountRequest;
import com.fund_transfer.backend.dto.Request.UpdateAccountStatusRequest;
import com.fund_transfer.backend.dto.Response.AccountBalanceResponse;
import com.fund_transfer.backend.dto.Response.AccountResponse;
import com.fund_transfer.backend.enums.AccountStatus;
import com.fund_transfer.backend.enums.AccountType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AccountServiceTest {

    @Autowired
    private AccountService accountService;

}
