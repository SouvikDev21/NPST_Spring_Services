package com.account_service.backend.controller;

import com.account_service.backend.dto.account.AccountDto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
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
        SearchRequest request = SearchRequest.builder()
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
        DetailsRequest request = DetailsRequest.builder()
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
        BalanceRequest request = BalanceRequest.builder()
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
        MiniStatementRequest request = MiniStatementRequest.builder()
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
        TransactionsRequest request = TransactionsRequest.builder()
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
        BeneficiariesRequest request = BeneficiariesRequest.builder()
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
        LinkedCardsRequest request = LinkedCardsRequest.builder()
                .accountNumber("101000000001")
                .build();

        mockMvc.perform(post("/api/v1/accounts/linked-cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
