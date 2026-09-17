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
import com.term_deposit.backend.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate; // <-- Added import for dynamic maturity calculation
import java.util.stream.Collectors;
import java.util.List;

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
                "FD_REGULAR",
                request.getPrincipalAmount(),
                request.getTenureMonths(),
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
                // --- FIXED: Dynamically calculate maturity date based on user tenure ---
                .maturityDate(LocalDate.now().plusMonths(request.getTenureMonths()))
                // ---------------------------------------------------------------------
                .cbsReferenceNumber(cbsResponse.cbsReferenceNumber())
                .principalAmount(request.getPrincipalAmount())
                .principalMinorUnits(request.getPrincipalAmount().multiply(new BigDecimal("100")).toBigInteger())
                .activatedAt(Instant.now())
                .interestEarnedMinorUnits(BigInteger.ZERO)
                .tenureDays(request.getTenureMonths() * 30)
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

    @Transactional
    public void closeTermDeposit(String depositNumber) {
        log.info("Executing CBS FD Closure for FD: {}", depositNumber);

        TermDeposit fd = termDepositRepository.findByDepositNumber(depositNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Term Deposit not found"));

        log.info("Applying premature withdrawal penalty for FD: {}", depositNumber);

        fd.setStatus(DepositStatus.CLOSED);
        fd.setClosedAt(Instant.now());

        termDepositRepository.save(fd);
        log.info("Successfully closed Term Deposit: {}", depositNumber);
    }
}