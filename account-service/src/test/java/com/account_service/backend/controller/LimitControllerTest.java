package com.account_service.backend.controller;

import com.account_service.backend.dto.limit.LimitDto.*;
import com.account_service.backend.enums.LimitChannel;
import com.account_service.backend.enums.LimitStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class LimitControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    // --- Channel Limits ---

    @Test
    void testChannelLimitSearch() throws Exception {
        ChannelSearchRequest request = ChannelSearchRequest.builder()
                .channel(LimitChannel.MOBILE)
                .page(0)
                .size(5)
                .build();

        mockMvc.perform(post("/api/v1/limits/channel/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testChannelLimitDetails() throws Exception {
        ChannelDetailsRequest request = ChannelDetailsRequest.builder()
                .channel(LimitChannel.MOBILE)
                .build();

        mockMvc.perform(post("/api/v1/limits/channel/details")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.channel").value("MOBILE"));
    }

    @Test
    void testChannelLimitCreateAndUpdate() throws Exception {
        CreateChannelRequest createReq = CreateChannelRequest.builder()
                .channel(LimitChannel.BRANCH)
                .perTransactionLimit(new BigDecimal("100000.00"))
                .dailyLimit(new BigDecimal("500000.00"))
                .monthlyLimit(new BigDecimal("5000000.00"))
                .maxTransactionsPerDay(20)
                .currency("INR")
                .build();

        mockMvc.perform(post("/api/v1/limits/channel/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.channel").value("BRANCH"));

        UpdateChannelRequest updateReq = UpdateChannelRequest.builder()
                .channel(LimitChannel.BRANCH)
                .perTransactionLimit(new BigDecimal("150000.00"))
                .status(LimitStatus.ACTIVE)
                .build();

        mockMvc.perform(post("/api/v1/limits/channel/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.perTransactionLimit").value(150000.00));
    }

    @Test
    void testChannelLimitHistory() throws Exception {
        ChannelHistoryRequest request = ChannelHistoryRequest.builder()
                .channel(LimitChannel.MOBILE)
                .page(0)
                .size(5)
                .build();

        mockMvc.perform(post("/api/v1/limits/channel/history")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // --- Customer Limits ---

    @Test
    void testCustomerLimitSearch() throws Exception {
        CustomerSearchRequest request = CustomerSearchRequest.builder()
                .customerId("CIF100001")
                .page(0)
                .size(5)
                .build();

        mockMvc.perform(post("/api/v1/limits/customer/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testCustomerLimitDetails() throws Exception {
        CustomerDetailsRequest request = CustomerDetailsRequest.builder()
                .customerId("CIF100001")
                .channel(LimitChannel.ALL)
                .build();

        mockMvc.perform(post("/api/v1/limits/customer/details")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.customerId").value("CIF100001"));
    }

    @Test
    void testCustomerLimitCreateAndUpdate() throws Exception {
        CreateCustomerRequest createReq = CreateCustomerRequest.builder()
                .customerId("CIF100001")
                .channel(LimitChannel.UPI)
                .perTransactionLimit(new BigDecimal("10000.00"))
                .dailyLimit(new BigDecimal("50000.00"))
                .monthlyLimit(new BigDecimal("500000.00"))
                .maxTransactionsPerDay(10)
                .currency("INR")
                .build();

        mockMvc.perform(post("/api/v1/limits/customer/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.channel").value("UPI"));

        UpdateCustomerRequest updateReq = UpdateCustomerRequest.builder()
                .customerId("CIF100001")
                .channel(LimitChannel.UPI)
                .perTransactionLimit(new BigDecimal("20000.00"))
                .status(LimitStatus.ACTIVE)
                .build();

        mockMvc.perform(post("/api/v1/limits/customer/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.perTransactionLimit").value(20000.00));
    }

    @Test
    void testCustomerLimitHistory() throws Exception {
        CustomerHistoryRequest request = CustomerHistoryRequest.builder()
                .customerId("CIF100001")
                .page(0)
                .size(5)
                .build();

        mockMvc.perform(post("/api/v1/limits/customer/history")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // --- Global Limits ---

    @Test
    void testGlobalLimitSearch() throws Exception {
        GlobalSearchRequest request = GlobalSearchRequest.builder()
                .limitCode("GLOBAL_DEF_001")
                .page(0)
                .size(5)
                .build();

        mockMvc.perform(post("/api/v1/limits/global/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testGlobalLimitDetails() throws Exception {
        GlobalDetailsRequest request = GlobalDetailsRequest.builder()
                .limitCode("GLOBAL_DEF_001")
                .build();

        mockMvc.perform(post("/api/v1/limits/global/details")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.limitCode").value("GLOBAL_DEF_001"));
    }

    @Test
    void testGlobalLimitCreateAndUpdate() throws Exception {
        CreateGlobalRequest createReq = CreateGlobalRequest.builder()
                .limitCode("GLOBAL_VIP_002")
                .limitName("VIP Tier Limit Policy")
                .perTransactionLimit(new BigDecimal("500000.00"))
                .dailyLimit(new BigDecimal("2000000.00"))
                .monthlyLimit(new BigDecimal("20000000.00"))
                .coolingPeriodHours(12)
                .currency("INR")
                .description("VIP limits policy")
                .build();

        mockMvc.perform(post("/api/v1/limits/global/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.limitCode").value("GLOBAL_VIP_002"));

        UpdateGlobalRequest updateReq = UpdateGlobalRequest.builder()
                .limitCode("GLOBAL_VIP_002")
                .perTransactionLimit(new BigDecimal("600000.00"))
                .status(LimitStatus.ACTIVE)
                .description("VIP limit updated")
                .build();

        mockMvc.perform(post("/api/v1/limits/global/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.perTransactionLimit").value(600000.00));
    }

    @Test
    void testGlobalLimitHistory() throws Exception {
        GlobalHistoryRequest request = GlobalHistoryRequest.builder()
                .limitCode("GLOBAL_DEF_001")
                .page(0)
                .size(5)
                .build();

        mockMvc.perform(post("/api/v1/limits/global/history")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
