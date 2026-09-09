package com.fund_transfer.backend.controller;

import com.fund_transfer.backend.dto.Request.CreateAccountRequest;
import com.fund_transfer.backend.dto.Request.UpdateAccountStatusRequest;
import com.fund_transfer.backend.dto.Response.AccountBalanceResponse;
import com.fund_transfer.backend.dto.Response.AccountResponse;
import com.fund_transfer.backend.dto.Response.ApiResponse;
import com.fund_transfer.backend.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {


}
