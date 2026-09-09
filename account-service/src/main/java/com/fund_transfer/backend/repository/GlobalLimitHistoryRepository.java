package com.fund_transfer.backend.repository;

import com.fund_transfer.backend.entity.GlobalLimitHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface GlobalLimitHistoryRepository extends JpaRepository<GlobalLimitHistory, UUID> {

    Page<GlobalLimitHistory> findByLimitCodeOrderByTimestampDesc(String limitCode, Pageable pageable);

    Page<GlobalLimitHistory> findAllByOrderByTimestampDesc(Pageable pageable);
}
