package com.term_deposit.backend.repository;

import com.term_deposit.backend.entity.TermDeposit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TermDepositRepository extends JpaRepository<TermDeposit, UUID> {

    // For your GET API
    List<TermDeposit> findByCif(String cif);

    // For your Premature Closure API
    Optional<TermDeposit> findByDepositNumber(String depositNumber);
}