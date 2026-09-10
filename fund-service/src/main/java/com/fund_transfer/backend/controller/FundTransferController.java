package com.fund_transfer.backend.controller;

import com.fund_transfer.backend.dto.Request.InitiateTransferRequest;
import com.fund_transfer.backend.dto.Response.TransactionResponse;

import com.fund_transfer.backend.service.FundTransferService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fund-transfer")
public class FundTransferController {

    private final FundTransferService fundTransferService;

    public FundTransferController(FundTransferService fundTransferService) {
        this.fundTransferService = fundTransferService;
    }

    @PostMapping("/initiate")
    public ResponseEntity<TransactionResponse> initiateTransfer(@RequestBody InitiateTransferRequest request) {
        return ResponseEntity.ok(fundTransferService.initiateTransfer(request));
    }
}
