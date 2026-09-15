package com.term_deposit.backend.repository;

import com.term_deposit.backend.entity.IdempotencyRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface IdempotencyRepository extends JpaRepository<IdempotencyRecord, UUID> {
    boolean existsByIdempotencyKey(String idempotencyKey);
}