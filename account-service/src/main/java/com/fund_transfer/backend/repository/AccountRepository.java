package com.fund_transfer.backend.repository;

import com.fund_transfer.backend.entity.Account;
import com.fund_transfer.backend.enums.AccountStatus;
import com.fund_transfer.backend.enums.AccountType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {

    Optional<Account> findByAccountNumber(String accountNumber);

    List<Account> findByCustomerId(String customerId);

    boolean existsByAccountNumber(String accountNumber);

    Page<Account> findByCustomerId(String customerId, Pageable pageable);

    @Query("SELECT a FROM Account a WHERE " +
           "(:customerId IS NULL OR a.customerId = :customerId) AND " +
           "(:accountNumber IS NULL OR a.accountNumber = :accountNumber) AND " +
           "(:accountType IS NULL OR a.accountType = :accountType) AND " +
           "(:status IS NULL OR a.status = :status)")
    Page<Account> searchAccounts(
            @Param("customerId") String customerId,
            @Param("accountNumber") String accountNumber,
            @Param("accountType") AccountType accountType,
            @Param("status") AccountStatus status,
            Pageable pageable
    );
}
