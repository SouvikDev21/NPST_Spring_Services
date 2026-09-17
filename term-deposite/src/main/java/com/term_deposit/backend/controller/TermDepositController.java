package com.term_deposit.backend.controller;

import com.term_deposit.backend.dto.Request.CloseDepositRequest;
import com.term_deposit.backend.dto.Request.OpenDepositRequest;
import com.term_deposit.backend.dto.Request.ApproveRequestDto;
import com.term_deposit.backend.dto.Response.TermDepositResponse;
import com.term_deposit.backend.enums.DepositRequestStatus;
import com.term_deposit.backend.enums.DepositRequestType;
import com.term_deposit.backend.entity.DepositRequest;
import com.term_deposit.backend.entity.TermDepositProduct;
import com.term_deposit.backend.repository.DepositRequestRepository;
import com.term_deposit.backend.repository.TermDepositProductRepository;
import com.term_deposit.backend.service.DepositRequestApprovalService;
import com.term_deposit.backend.service.TermDepositService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.BigInteger; // <-- Added this import for minor units math
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/term-deposits")
@RequiredArgsConstructor
public class TermDepositController {

    private final DepositRequestApprovalService approvalService;
    private final DepositRequestRepository depositRequestRepository;
    private final TermDepositService termDepositService;
    private final TermDepositProductRepository productRepository;

    /**
     * MAKER ENDPOINT: Initiates the request.
     * Does NOT move money. Saves to database as UNDER_REVIEW.
     */
    @PostMapping("/requests")
    public ResponseEntity<String> initiateDepositRequest(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody OpenDepositRequest requestDto) {

        log.info("Received request to open FD for CIF: {}", requestDto.cif());

        // 1. Fetch using the updated Repository method (productCode instead of productId)
        TermDepositProduct productRules = productRepository.findByProductCode("FD_REGULAR")
                .orElseThrow(() -> new RuntimeException("Product catalog rules not found for FD_REGULAR"));

        // 2. Dynamic Business Rule Validation: Amount
        // Convert user's BigDecimal (e.g. 5000.00) to minor units BigInteger (e.g. 500000) for database comparison
        BigInteger requestedMinorUnits = requestDto.principalAmount().multiply(new BigDecimal("100")).toBigInteger();

        if (requestedMinorUnits.compareTo(productRules.getMinAmountMinorUnits()) < 0 ||
                requestedMinorUnits.compareTo(productRules.getMaxAmountMinorUnits()) > 0) {

            // Convert back to readable major units for the error message
            BigDecimal minDisplayAmount = new BigDecimal(productRules.getMinAmountMinorUnits()).divide(new BigDecimal("100"));
            BigDecimal maxDisplayAmount = new BigDecimal(productRules.getMaxAmountMinorUnits()).divide(new BigDecimal("100"));

            return ResponseEntity.badRequest().body("Error: Principal amount must be between ₹"
                    + minDisplayAmount + " and ₹" + maxDisplayAmount);
        }

        // 3. Dynamic Business Rule Validation: Tenure
        if (requestDto.tenureMonths() < productRules.getMinTenureMonths() ||
                requestDto.tenureMonths() > productRules.getMaxTenureMonths()) {
            return ResponseEntity.badRequest().body("Error: Tenure must be between "
                    + productRules.getMinTenureMonths() + " and " + productRules.getMaxTenureMonths() + " months.");
        }

        // 4. Save as a pending request for the Checker to review
        DepositRequest pendingRequest = DepositRequest.builder()
                .cif(requestDto.cif())
                .makerKeycloakUserId(requestDto.makerUserId())
                .requestType(DepositRequestType.CREATE_FIXED_DEPOSIT)
                .status(DepositRequestStatus.UNDER_REVIEW)
                .principalAmount(requestDto.principalAmount())
                .tenureMonths(requestDto.tenureMonths())
                .remarks("Amount: " + requestDto.principalAmount() + ", Tenure: " + requestDto.tenureMonths())
                .build();

        depositRequestRepository.save(pendingRequest);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Request submitted successfully. Pending Checker approval. Reference ID: " + pendingRequest.getId());
    }

    /**
     * CHECKER ENDPOINT: Approves the request.
     * Triggers CBS integration and finalizes the Term Deposit.
     */
    @PostMapping("/requests/{requestId}/approve")
    public ResponseEntity<String> approveDepositRequest(
            @PathVariable UUID requestId,
            @Valid @RequestBody ApproveRequestDto approveDto) {

        log.info("Received approval submission for Request ID: {}", requestId);

        // This routes into your segregated Service layer logic
        approvalService.approveRequest(requestId, approveDto.checkerUserId());

        return ResponseEntity.ok("Deposit Request approved and FD successfully created in Core Banking System.");
    }

    @GetMapping
    public ResponseEntity<List<TermDepositResponse>> getTermDeposits(@RequestParam String cif) {
        List<TermDepositResponse> response = termDepositService.getCustomerTermDeposits(cif);
        return ResponseEntity.ok(response);
    }

    /**
     * MAKER ENDPOINT: Initiates Premature Closure.
     * Does NOT close the FD. Saves to database as UNDER_REVIEW.
     */
    @PostMapping("/requests/close")
    public ResponseEntity<String> initiateClosureRequest(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody CloseDepositRequest requestDto) {

        log.info("Received request to close FD {} for CIF: {}", requestDto.depositNumber(), requestDto.cif());

        DepositRequest pendingRequest = DepositRequest.builder()
                .cif(requestDto.cif())
                .makerKeycloakUserId(requestDto.makerUserId())
                .requestType(DepositRequestType.PREMATURE_CLOSURE)
                .status(DepositRequestStatus.UNDER_REVIEW)
                .requestReference(requestDto.depositNumber())
                .principalAmount(BigDecimal.ZERO)
                .tenureMonths(0)
                .remarks("Premature closure requested for FD: " + requestDto.depositNumber())
                .build();

        depositRequestRepository.save(pendingRequest);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Closure request submitted successfully. Pending Checker approval. Reference ID: " + pendingRequest.getId());
    }
}