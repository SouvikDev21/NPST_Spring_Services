package com.fund_transfer.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fund_transfer.backend.dto.request.statement.StatementDownloadRequest;
import com.fund_transfer.backend.dto.request.statement.StatementEmailRequest;
import com.fund_transfer.backend.dto.request.statement.StatementSearchRequest;
import com.fund_transfer.backend.enums.StatementFormat;
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
class StatementControllerTest {

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
    void testStatementSearch() throws Exception {
        StatementSearchRequest request = StatementSearchRequest.builder()
                .accountNumber("101000000001")
                .fromDate(LocalDate.now().minusDays(30))
                .toDate(LocalDate.now())
                .page(0)
                .size(10)
                .build();

        mockMvc.perform(post("/api/v1/statements/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testStatementDownload() throws Exception {
        StatementDownloadRequest request = StatementDownloadRequest.builder()
                .accountNumber("101000000001")
                .fromDate(LocalDate.now().minusDays(30))
                .toDate(LocalDate.now())
                .format(StatementFormat.PDF)
                .build();

        mockMvc.perform(post("/api/v1/statements/download")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fileContentBase64").isString());
    }

    @Test
    void testStatementEmail() throws Exception {
        StatementEmailRequest request = StatementEmailRequest.builder()
                .accountNumber("101000000001")
                .emailAddress("user@example.com")
                .fromDate(LocalDate.now().minusDays(30))
                .toDate(LocalDate.now())
                .format(StatementFormat.PDF)
                .build();

        mockMvc.perform(post("/api/v1/statements/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.recipientEmail").value("user@example.com"));
    }
}
