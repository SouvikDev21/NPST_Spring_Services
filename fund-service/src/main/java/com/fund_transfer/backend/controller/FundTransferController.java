package com.fund_transfer.backend.controller;

import com.fund_transfer.backend.dto.Request.TransferRequest;
import com.fund_transfer.backend.dto.Response.TransactionResponse;

import com.fund_transfer.backend.service.TransactionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fund-transfer")
@RequiredArgsConstructor
public class FundTransferController {
    private final TransactionService transactionService;
    @PostMapping("/initiate")
    public ResponseEntity<TransactionResponse> transfer(
            @RequestHeader("Idempotency-Key") @NotBlank String idempotencyKey,
            @Valid @RequestBody TransferRequest request) {

        TransactionResponse response = transactionService.processTransfer(request, idempotencyKey);
        return ResponseEntity.ok(response);
    }
}
