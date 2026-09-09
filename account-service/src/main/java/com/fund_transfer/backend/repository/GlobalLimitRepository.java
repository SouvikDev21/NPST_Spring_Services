package com.fund_transfer.backend.repository;

import com.fund_transfer.backend.entity.GlobalLimit;
import com.fund_transfer.backend.enums.LimitStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface GlobalLimitRepository extends JpaRepository<GlobalLimit, UUID> {

    Optional<GlobalLimit> findByLimitCode(String limitCode);

    boolean existsByLimitCode(String limitCode);

    @Query("SELECT gl FROM GlobalLimit gl WHERE " +
           "(:limitCode IS NULL OR LOWER(gl.limitCode) LIKE LOWER(CONCAT('%', :limitCode, '%'))) AND " +
           "(:status IS NULL OR gl.status = :status)")
    Page<GlobalLimit> searchGlobalLimits(
            @Param("limitCode") String limitCode,
            @Param("status") LimitStatus status,
            Pageable pageable
    );
}
