package com.fund_transfer.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fund_transfer.backend.dto.request.limit.ChannelLimitDetailsRequest;
import com.fund_transfer.backend.dto.request.limit.ChannelLimitHistoryRequest;
import com.fund_transfer.backend.dto.request.limit.ChannelLimitSearchRequest;
import com.fund_transfer.backend.dto.request.limit.CreateChannelLimitRequest;
import com.fund_transfer.backend.dto.request.limit.CreateCustomerLimitRequest;
import com.fund_transfer.backend.dto.request.limit.CreateGlobalLimitRequest;
import com.fund_transfer.backend.dto.request.limit.CustomerLimitDetailsRequest;
import com.fund_transfer.backend.dto.request.limit.CustomerLimitHistoryRequest;
import com.fund_transfer.backend.dto.request.limit.CustomerLimitSearchRequest;
import com.fund_transfer.backend.dto.request.limit.GlobalLimitDetailsRequest;
import com.fund_transfer.backend.dto.request.limit.GlobalLimitHistoryRequest;
import com.fund_transfer.backend.dto.request.limit.GlobalLimitSearchRequest;
import com.fund_transfer.backend.dto.request.limit.UpdateChannelLimitRequest;
import com.fund_transfer.backend.dto.request.limit.UpdateCustomerLimitRequest;
import com.fund_transfer.backend.dto.request.limit.UpdateGlobalLimitRequest;
import com.fund_transfer.backend.enums.LimitChannel;
import com.fund_transfer.backend.enums.LimitStatus;
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
        ChannelLimitSearchRequest request = ChannelLimitSearchRequest.builder()
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
        ChannelLimitDetailsRequest request = ChannelLimitDetailsRequest.builder()
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
        CreateChannelLimitRequest createReq = CreateChannelLimitRequest.builder()
                .channel(LimitChannel.BRANCH)
                .perTransactionLimit(new BigDecimal("100000.00"))
                .dailyLimit(new BigDecimal("500000.00"))
                .monthlyLimit(new BigDecimal("5000000.00"))
                .maxTransactionsPerDay(20)
                .currency("INR")
                .remarks("Branch limit policy")
                .build();

        mockMvc.perform(post("/api/v1/limits/channel/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.channel").value("BRANCH"));

        UpdateChannelLimitRequest updateReq = UpdateChannelLimitRequest.builder()
                .channel(LimitChannel.BRANCH)
                .perTransactionLimit(new BigDecimal("150000.00"))
                .status(LimitStatus.ACTIVE)
                .remarks("Branch limit raised")
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
        ChannelLimitHistoryRequest request = ChannelLimitHistoryRequest.builder()
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
        CustomerLimitSearchRequest request = CustomerLimitSearchRequest.builder()
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
        CustomerLimitDetailsRequest request = CustomerLimitDetailsRequest.builder()
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
        CreateCustomerLimitRequest createReq = CreateCustomerLimitRequest.builder()
                .customerId("CIF100001")
                .channel(LimitChannel.UPI)
                .perTransactionLimit(new BigDecimal("10000.00"))
                .dailyLimit(new BigDecimal("50000.00"))
                .monthlyLimit(new BigDecimal("500000.00"))
                .maxTransactionsPerDay(10)
                .currency("INR")
                .remarks("Custom UPI Limit")
                .build();

        mockMvc.perform(post("/api/v1/limits/customer/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.channel").value("UPI"));

        UpdateCustomerLimitRequest updateReq = UpdateCustomerLimitRequest.builder()
                .customerId("CIF100001")
                .channel(LimitChannel.UPI)
                .perTransactionLimit(new BigDecimal("20000.00"))
                .status(LimitStatus.ACTIVE)
                .remarks("UPI limit updated")
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
        CustomerLimitHistoryRequest request = CustomerLimitHistoryRequest.builder()
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
        GlobalLimitSearchRequest request = GlobalLimitSearchRequest.builder()
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
        GlobalLimitDetailsRequest request = GlobalLimitDetailsRequest.builder()
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
        CreateGlobalLimitRequest createReq = CreateGlobalLimitRequest.builder()
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

        UpdateGlobalLimitRequest updateReq = UpdateGlobalLimitRequest.builder()
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
        GlobalLimitHistoryRequest request = GlobalLimitHistoryRequest.builder()
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
