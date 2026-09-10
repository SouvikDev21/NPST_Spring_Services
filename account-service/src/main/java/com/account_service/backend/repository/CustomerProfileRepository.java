package com.account_service.backend.repository;

import com.account_service.backend.entity.CustomerProfile;
import com.account_service.backend.enums.KycStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerProfileRepository extends JpaRepository<CustomerProfile, UUID> {

    Optional<CustomerProfile> findByCustomerId(String customerId);

    boolean existsByCustomerId(String customerId);

    @Query("SELECT c FROM CustomerProfile c WHERE " +
           "(:searchKey IS NULL OR LOWER(c.customerName) LIKE LOWER(CONCAT('%', :searchKey, '%')) OR LOWER(c.customerId) LIKE LOWER(CONCAT('%', :searchKey, '%')) OR LOWER(c.mobileNumber) LIKE LOWER(CONCAT('%', :searchKey, '%'))) AND " +
           "(:kycStatus IS NULL OR c.kycStatus = :kycStatus) AND " +
           "(:customerType IS NULL OR LOWER(c.customerType) = LOWER(:customerType))")
    Page<CustomerProfile> searchCustomers(
            @Param("searchKey") String searchKey,
            @Param("kycStatus") KycStatus kycStatus,
            @Param("customerType") String customerType,
            Pageable pageable
    );
}
