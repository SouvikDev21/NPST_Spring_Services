package com.fund_transfer.backend.repository;

import com.fund_transfer.backend.entity.Beneficiary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BeneficiaryRepository extends JpaRepository<Beneficiary, UUID> {

    List<Beneficiary> findByAccountNumber(String accountNumber);

    List<Beneficiary> findByCustomerId(String customerId);

    List<Beneficiary> findByAccountNumberOrCustomerId(String accountNumber, String customerId);
}
