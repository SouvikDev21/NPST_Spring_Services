package com.account_service.backend.repository;

import com.account_service.backend.entity.ChannelLimit;
import com.account_service.backend.enums.LimitChannel;
import com.account_service.backend.enums.LimitStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChannelLimitRepository extends JpaRepository<ChannelLimit, UUID> {

    Optional<ChannelLimit> findByChannel(LimitChannel channel);

    Page<ChannelLimit> findByChannel(LimitChannel channel, Pageable pageable);

    Optional<ChannelLimit> findByChannelAndStatus(LimitChannel channel, LimitStatus status);

    boolean existsByChannel(LimitChannel channel);

    boolean existsByChannelAndStatus(LimitChannel channel, LimitStatus status);

    Page<ChannelLimit> findByStatus(LimitStatus status, Pageable pageable);

    Page<ChannelLimit> findByChannelAndStatus(LimitChannel channel, LimitStatus status, Pageable pageable);
}
