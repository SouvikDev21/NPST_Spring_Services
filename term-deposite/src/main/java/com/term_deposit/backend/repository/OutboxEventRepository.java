package com.term_deposit.backend.repository;

import com.term_deposit.backend.entity.OutboxEvent;
import com.term_deposit.backend.enums.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    // Custom method to find all events waiting to be processed
    List<OutboxEvent> findByStatus(OutboxStatus status);
}