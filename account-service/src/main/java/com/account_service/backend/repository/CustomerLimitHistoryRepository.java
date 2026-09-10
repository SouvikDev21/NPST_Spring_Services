package com.account_service.backend.repository;

import com.account_service.backend.entity.CustomerLimitHistory;
import com.account_service.backend.enums.LimitChannel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CustomerLimitHistoryRepository extends JpaRepository<CustomerLimitHistory, UUID> {

    Page<CustomerLimitHistory> findAllByOrderByTimestampDesc(Pageable pageable);

    Page<CustomerLimitHistory> findByCustomerIdOrderByTimestampDesc(String customerId, Pageable pageable);

    Page<CustomerLimitHistory> findByCustomerIdAndChannelOrderByTimestampDesc(String customerId, LimitChannel channel, Pageable pageable);
}
