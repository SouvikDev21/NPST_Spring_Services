package com.account_service.backend.repository;

import com.account_service.backend.entity.AccountTransaction;
import com.account_service.backend.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface AccountTransactionRepository extends JpaRepository<AccountTransaction, UUID> {

    List<AccountTransaction> findTop10ByAccountNumberOrderByTransactionDateDescCreatedAtDesc(String accountNumber);

    @Query("SELECT t FROM AccountTransaction t WHERE t.accountNumber = :accountNumber " +
           "AND (:fromDate IS NULL OR t.transactionDate >= :fromDate) " +
           "AND (:toDate IS NULL OR t.transactionDate <= :toDate) " +
           "AND (:transactionType IS NULL OR t.transactionType = :transactionType)")
    Page<AccountTransaction> searchTransactions(
            @Param("accountNumber") String accountNumber,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("transactionType") TransactionType transactionType,
            Pageable pageable
    );
}
