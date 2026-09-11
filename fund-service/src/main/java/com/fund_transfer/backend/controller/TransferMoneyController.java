package com.fund_transfer.backend.controller;

import com.fund_transfer.backend.dto.Request.TransferRequest;
import com.fund_transfer.backend.dto.Response.TransactionResponse;
import com.fund_transfer.backend.entity.Transaction;
import com.fund_transfer.backend.service.TransactionService;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/transfer")
@RequiredArgsConstructor
public class TransferMoneyController {
    private final TransactionService transactionService;

    /*
     api  needed ->
     1. transfer money
     2.see transaction history
     3. payment history related to a beneficiary
     */
    /*
            transfer money flow
            1.ui sends request with ownercif,owner accno,beneficiary id ,accno,ifsc,amount etc.
            2.backend sends request to cbs to debit user(implement check balance logic)
            3.mock the npci request disbursal and confirmation(with random success failure)
            4.based on the response set transaction status and send response
     */

    @PostMapping("/sendMoney")
    public ResponseEntity<TransactionResponse> sendMoney(@Valid @RequestBody TransferRequest request) {
        TransactionResponse response = transactionService.processTransfer(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}



