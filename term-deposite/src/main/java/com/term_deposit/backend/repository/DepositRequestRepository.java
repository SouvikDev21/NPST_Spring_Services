package com.term_deposit.backend.repository;

import com.term_deposit.backend.entity.DepositRequest;
import com.term_deposit.backend.enums.DepositRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DepositRequestRepository extends JpaRepository<DepositRequest, UUID> {
    Optional<DepositRequest> findByRequestReference(String requestReference);
    List<DepositRequest> findByStatus(DepositRequestStatus status);
}