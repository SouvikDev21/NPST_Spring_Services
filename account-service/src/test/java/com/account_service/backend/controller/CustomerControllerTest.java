package com.account_service.backend.controller;

import com.account_service.backend.dto.customer.CustomerDto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class CustomerControllerTest {

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
    void testCustomerSummary() throws Exception {
        CustomerRequest request = CustomerRequest.builder()
                .customerId("CIF100001")
                .build();

        mockMvc.perform(post("/api/v1/customer/summary")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.customerId").value("CIF100001"));
    }

    @Test
    void testCustomersSearch() throws Exception {
        SearchRequest request = SearchRequest.builder()
                .searchKey("John")
                .page(0)
                .size(10)
                .build();

        mockMvc.perform(post("/api/v1/customers/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testCustomerDetails() throws Exception {
        CustomerRequest request = CustomerRequest.builder()
                .customerId("CIF100001")
                .build();

        mockMvc.perform(post("/api/v1/customers/details")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.customerId").value("CIF100001"));
    }

    @Test
    void testCustomerAccounts() throws Exception {
        AccountsRequest request = AccountsRequest.builder()
                .customerId("CIF100001")
                .page(0)
                .size(10)
                .build();

        mockMvc.perform(post("/api/v1/customers/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testCustomerKycDetails() throws Exception {
        CustomerRequest request = CustomerRequest.builder()
                .customerId("CIF100001")
                .build();

        mockMvc.perform(post("/api/v1/customers/kyc-details")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.customerId").value("CIF100001"));
    }

    @Test
    void testCustomerRelationshipDetails() throws Exception {
        CustomerRequest request = CustomerRequest.builder()
                .customerId("CIF100001")
                .build();

        mockMvc.perform(post("/api/v1/customers/relationship-details")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.customerId").value("CIF100001"));
    }
}
