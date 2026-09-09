package com.fund_transfer.backend.repository;

import com.fund_transfer.backend.entity.ChannelLimit;
import com.fund_transfer.backend.enums.LimitChannel;
import com.fund_transfer.backend.enums.LimitStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChannelLimitRepository extends JpaRepository<ChannelLimit, UUID> {

    Optional<ChannelLimit> findByChannel(LimitChannel channel);

    boolean existsByChannel(LimitChannel channel);

    Page<ChannelLimit> findByStatus(LimitStatus status, Pageable pageable);

    Page<ChannelLimit> findByChannelAndStatus(LimitChannel channel, LimitStatus status, Pageable pageable);
}
