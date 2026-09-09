package com.fund_transfer.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fund_transfer.backend.dto.request.account.AccountBalanceRequest;
import com.fund_transfer.backend.dto.request.account.AccountBeneficiariesRequest;
import com.fund_transfer.backend.dto.request.account.AccountDetailsRequest;
import com.fund_transfer.backend.dto.request.account.AccountLinkedCardsRequest;
import com.fund_transfer.backend.dto.request.account.AccountMiniStatementRequest;
import com.fund_transfer.backend.dto.request.account.AccountSearchRequest;
import com.fund_transfer.backend.dto.request.account.AccountTransactionsRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class AccountControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void testAccountSearch() throws Exception {
        AccountSearchRequest request = AccountSearchRequest.builder()
                .customerId("CIF100001")
                .page(0)
                .size(5)
                .build();

        mockMvc.perform(post("/api/v1/accounts/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testAccountDetails() throws Exception {
        AccountDetailsRequest request = AccountDetailsRequest.builder()
                .accountNumber("101000000001")
                .build();

        mockMvc.perform(post("/api/v1/accounts/details")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accountNumber").value("101000000001"));
    }

    @Test
    void testAccountBalance() throws Exception {
        AccountBalanceRequest request = AccountBalanceRequest.builder()
                .accountNumber("101000000001")
                .build();

        mockMvc.perform(post("/api/v1/accounts/balance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accountNumber").value("101000000001"));
    }

    @Test
    void testAccountMiniStatement() throws Exception {
        AccountMiniStatementRequest request = AccountMiniStatementRequest.builder()
                .accountNumber("101000000001")
                .count(5)
                .build();

        mockMvc.perform(post("/api/v1/accounts/mini-statement")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accountNumber").value("101000000001"));
    }

    @Test
    void testAccountTransactions() throws Exception {
        AccountTransactionsRequest request = AccountTransactionsRequest.builder()
                .accountNumber("101000000001")
                .fromDate(LocalDate.now().minusDays(30))
                .toDate(LocalDate.now())
                .page(0)
                .size(10)
                .build();

        mockMvc.perform(post("/api/v1/accounts/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testAccountBeneficiaries() throws Exception {
        AccountBeneficiariesRequest request = AccountBeneficiariesRequest.builder()
                .accountNumber("101000000001")
                .build();

        mockMvc.perform(post("/api/v1/accounts/beneficiaries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testAccountLinkedCards() throws Exception {
        AccountLinkedCardsRequest request = AccountLinkedCardsRequest.builder()
                .accountNumber("101000000001")
                .build();

        mockMvc.perform(post("/api/v1/accounts/linked-cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
