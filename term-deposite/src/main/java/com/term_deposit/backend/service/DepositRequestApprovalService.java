package com.term_deposit.backend.service;

import com.term_deposit.backend.dto.Mapper.TermDepositMapper;
import com.term_deposit.backend.enums.DepositRequestStatus;
import com.term_deposit.backend.enums.DepositRequestType; // ADDED: Missing import
import com.term_deposit.backend.exception.BusinessRuleViolationException;
import com.term_deposit.backend.exception.ResourceNotFoundException;
import com.term_deposit.backend.entity.DepositRequest;
import com.term_deposit.backend.repository.DepositRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepositRequestApprovalService {

    // FIXED: Renamed to match the variable used in your methods
    private final DepositRequestRepository depositRequestRepository;
    private final TermDepositService termDepositService;

    @Transactional
    public void approveRequest(UUID requestId, UUID checkerUserId) {
        // Fetch the existing request from the database using the ID
        DepositRequest depositRequest = depositRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found"));

        // ... (Keep your existing Segregation of Duties validation here) ...

        // ... (Keep your existing Segregation of Duties validation here) ...

        depositRequest.setStatus(DepositRequestStatus.APPROVED);
        depositRequest.setCheckerKeycloakUserId(checkerUserId);
        depositRequest.setReviewedAt(Instant.now());
        depositRequestRepository.save(depositRequest);
        // Route the action based on the Request Type
        if (depositRequest.getRequestType() == DepositRequestType.CREATE_FIXED_DEPOSIT) {
            // FIXED: Updated to the correct method name that exists in TermDepositService
            termDepositService.executeApprovedDepositCreation(depositRequest);
        } else if (depositRequest.getRequestType() == DepositRequestType.PREMATURE_CLOSURE) {
            // Our new closure logic!
            termDepositService.closeTermDeposit(depositRequest.getRequestReference());
        }
    }
}