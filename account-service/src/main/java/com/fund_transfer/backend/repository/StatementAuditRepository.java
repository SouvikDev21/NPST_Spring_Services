package com.fund_transfer.backend.repository;

import com.fund_transfer.backend.entity.StatementAudit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface StatementAuditRepository extends JpaRepository<StatementAudit, UUID> {

    Page<StatementAudit> findByAccountNumberOrderByTimestampDesc(String accountNumber, Pageable pageable);

    Page<StatementAudit> findByCustomerIdOrderByTimestampDesc(String customerId, Pageable pageable);
}
