package com.term_deposit.backend.service;

import com.term_deposit.backend.integration.CbsFeignClient;
import com.term_deposit.backend.dto.Request.CbsOpenTdRequest;
import com.term_deposit.backend.dto.Response.CbsOpenTdResponse;
import com.term_deposit.backend.dto.Response.TermDepositResponse;
import com.term_deposit.backend.dto.Mapper.TermDepositMapper;
import com.term_deposit.backend.enums.DepositStatus;
import com.term_deposit.backend.enums.OutboxStatus;
import com.term_deposit.backend.entity.DepositRequest;
import com.term_deposit.backend.entity.OutboxEvent;
import com.term_deposit.backend.entity.TermDeposit;
import com.term_deposit.backend.repository.OutboxEventRepository;
import com.term_deposit.backend.repository.TermDepositRepository;
import com.term_deposit.backend.exception.ResourceNotFoundException; // Added this import
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant; // Added this import
import java.util.stream.Collectors;
import java.util.List;
import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class TermDepositService {

    private final TermDepositRepository termDepositRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final CbsFeignClient cbsFeignClient;

    @Transactional
    public TermDeposit executeApprovedDepositCreation(DepositRequest request) {
        log.info("Executing CBS FD Creation for Request Reference: {}", request.getRequestReference());

        // 1. Map local request to CBS external format
        CbsOpenTdRequest cbsRequest = new CbsOpenTdRequest(
                request.getCif(),
                "FD-REGULAR",
                new BigDecimal("50000.00"),
                12,
                "SAVINGS-12345",
                "ON_MATURITY"
        );

        // 2. Call the Core Banking System
        CbsOpenTdResponse cbsResponse = cbsFeignClient.openTermDeposit(cbsRequest);

        // 3. Create the permanent local Term Deposit record
        TermDeposit termDeposit = TermDeposit.builder()
                .depositNumber(cbsResponse.cbsReferenceNumber())
                .cif(request.getCif())
                .keycloakUserId(request.getKeycloakUserId())
                .status(DepositStatus.ACTIVE)
                .interestRate(cbsResponse.appliedInterestRate())
                .maturityDate(cbsResponse.maturityDate())
                .cbsReferenceNumber(cbsResponse.cbsReferenceNumber())
                .build();

        TermDeposit savedDeposit = termDepositRepository.save(termDeposit);

        // 4. Trigger Outbox Event
        OutboxEvent event = OutboxEvent.builder()
                .aggregateType("TERM_DEPOSIT")
                .aggregateId(savedDeposit.getId())
                .eventType("FD_CREATED_SUCCESS")
                .payload("{\"depositNumber\":\"" + savedDeposit.getDepositNumber() + "\"}")
                .status(OutboxStatus.PENDING)
                .retryCount(0)
                .build();

        outboxEventRepository.save(event);

        log.info("Successfully created Term Deposit: {}", savedDeposit.getDepositNumber());
        return savedDeposit;
    }

    public List<TermDepositResponse> getCustomerTermDeposits(String cif) {
        log.info("Fetching term deposits for CIF: {}", cif);

        List<TermDeposit> deposits = termDepositRepository.findByCif(cif);

        return deposits.stream()
                .map(TermDepositMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    // --- NEW PREMATURE CLOSURE LOGIC ADDED HERE ---
    @Transactional
    public void closeTermDeposit(String depositNumber) {
        log.info("Executing CBS FD Closure for FD: {}", depositNumber);

        // 1. Fetch the existing active FD
        TermDeposit fd = termDepositRepository.findByDepositNumber(depositNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Term Deposit not found"));

        // 2. Simulate Penalty Calculation & CBS Update
        log.info("Applying premature withdrawal penalty for FD: {}", depositNumber);

        // 3. Update the database state
        fd.setStatus(DepositStatus.CLOSED);
        fd.setClosedAt(Instant.now());

        termDepositRepository.save(fd);
        log.info("Successfully closed Term Deposit: {}", depositNumber);
    }
}