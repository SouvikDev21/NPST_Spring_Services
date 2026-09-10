package com.account_service.backend.repository;

import com.account_service.backend.entity.ChannelLimitHistory;
import com.account_service.backend.enums.LimitChannel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ChannelLimitHistoryRepository extends JpaRepository<ChannelLimitHistory, UUID> {

    Page<ChannelLimitHistory> findByChannelOrderByTimestampDesc(LimitChannel channel, Pageable pageable);

    Page<ChannelLimitHistory> findAllByOrderByTimestampDesc(Pageable pageable);
}
