package com.account_service.backend.repository;

import com.account_service.backend.entity.CustomerLimit;
import com.account_service.backend.enums.LimitChannel;
import com.account_service.backend.enums.LimitStatus;
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
public interface CustomerLimitRepository extends JpaRepository<CustomerLimit, UUID> {

    Optional<CustomerLimit> findByCustomerIdAndChannel(String customerId, LimitChannel channel);

    Page<CustomerLimit> findByCustomerIdAndChannel(String customerId, LimitChannel channel, Pageable pageable);

    Optional<CustomerLimit> findByCustomerIdAndChannelAndStatus(String customerId, LimitChannel channel, LimitStatus status);

    boolean existsByCustomerIdAndChannelAndStatus(String customerId, LimitChannel channel, LimitStatus status);

    List<CustomerLimit> findByCustomerId(String customerId);

    Page<CustomerLimit> findByCustomerId(String customerId, Pageable pageable);

    @Query("SELECT cl FROM CustomerLimit cl WHERE " +
           "(:customerId IS NULL OR cl.customerId = :customerId) AND " +
           "(:channel IS NULL OR cl.channel = :channel) AND " +
           "(:status IS NULL OR cl.status = :status)")
    Page<CustomerLimit> searchCustomerLimits(
            @Param("customerId") String customerId,
            @Param("channel") LimitChannel channel,
            @Param("status") LimitStatus status,
            Pageable pageable
    );
}
