package com.fund_transfer.backend.repository;

import com.fund_transfer.backend.entity.CustomerLimitHistory;
import com.fund_transfer.backend.enums.LimitChannel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CustomerLimitHistoryRepository extends JpaRepository<CustomerLimitHistory, UUID> {

    Page<CustomerLimitHistory> findByCustomerIdOrderByTimestampDesc(String customerId, Pageable pageable);

    Page<CustomerLimitHistory> findByCustomerIdAndChannelOrderByTimestampDesc(String customerId, LimitChannel channel, Pageable pageable);
}
