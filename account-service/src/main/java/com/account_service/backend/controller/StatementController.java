package com.account_service.backend.controller;

import com.account_service.backend.dto.common.ApiResponse;
import com.account_service.backend.dto.statement.StatementDto.*;
import com.account_service.backend.service.StatementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/statements")
@RequiredArgsConstructor
public class StatementController {

    private final StatementService statementService;

    @PostMapping("/search")
    public ResponseEntity<ApiResponse<SearchResponse>> searchStatements(
            @Valid @RequestBody SearchRequest request
    ) {
        log.info("REST request to search statements for account: {}", request.getAccountNumber());
        SearchResponse response = statementService.searchStatements(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/download")
    public ResponseEntity<ApiResponse<DownloadResponse>> downloadStatement(
            @Valid @RequestBody DownloadRequest request
    ) {
        log.info("REST request to download statement for account: {}", request.getAccountNumber());
        DownloadResponse response = statementService.downloadStatement(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/email")
    public ResponseEntity<ApiResponse<EmailResponse>> emailStatement(
            @Valid @RequestBody EmailRequest request
    ) {
        log.info("REST request to email statement for account: {} to: {}", request.getAccountNumber(), request.getEmailAddress());
        EmailResponse response = statementService.emailStatement(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
