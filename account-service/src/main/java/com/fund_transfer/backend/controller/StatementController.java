package com.fund_transfer.backend.controller;

import com.fund_transfer.backend.dto.request.statement.StatementDownloadRequest;
import com.fund_transfer.backend.dto.request.statement.StatementEmailRequest;
import com.fund_transfer.backend.dto.request.statement.StatementSearchRequest;
import com.fund_transfer.backend.dto.common.ApiResponse;
import com.fund_transfer.backend.dto.response.statement.StatementDownloadResponse;
import com.fund_transfer.backend.dto.response.statement.StatementEmailResponse;
import com.fund_transfer.backend.dto.response.statement.StatementSearchResponse;
import com.fund_transfer.backend.service.StatementService;
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
    public ResponseEntity<ApiResponse<StatementSearchResponse>> searchStatements(
            @Valid @RequestBody StatementSearchRequest request
    ) {
        log.info("REST request to search statements for account: {}", request.getAccountNumber());
        StatementSearchResponse response = statementService.searchStatements(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/download")
    public ResponseEntity<ApiResponse<StatementDownloadResponse>> downloadStatement(
            @Valid @RequestBody StatementDownloadRequest request
    ) {
        log.info("REST request to download statement for account: {}", request.getAccountNumber());
        StatementDownloadResponse response = statementService.downloadStatement(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/email")
    public ResponseEntity<ApiResponse<StatementEmailResponse>> emailStatement(
            @Valid @RequestBody StatementEmailRequest request
    ) {
        log.info("REST request to email statement for account: {} to: {}", request.getAccountNumber(), request.getEmailAddress());
        StatementEmailResponse response = statementService.emailStatement(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
