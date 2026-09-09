package com.fund_transfer.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fund_transfer.backend.dto.request.customer.CustomerAccountsRequest;
import com.fund_transfer.backend.dto.request.customer.CustomerDetailsRequest;
import com.fund_transfer.backend.dto.request.customer.CustomerKycDetailsRequest;
import com.fund_transfer.backend.dto.request.customer.CustomerRelationshipDetailsRequest;
import com.fund_transfer.backend.dto.request.customer.CustomerSearchRequest;
import com.fund_transfer.backend.dto.request.customer.CustomerSummaryRequest;
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
        CustomerSummaryRequest request = CustomerSummaryRequest.builder()
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
        CustomerSearchRequest request = CustomerSearchRequest.builder()
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
        CustomerDetailsRequest request = CustomerDetailsRequest.builder()
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
        CustomerAccountsRequest request = CustomerAccountsRequest.builder()
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
        CustomerKycDetailsRequest request = CustomerKycDetailsRequest.builder()
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
        CustomerRelationshipDetailsRequest request = CustomerRelationshipDetailsRequest.builder()
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
