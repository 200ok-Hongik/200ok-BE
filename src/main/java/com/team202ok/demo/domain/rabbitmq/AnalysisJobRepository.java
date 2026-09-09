package com.team202ok.demo.domain.rabbitmq;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface AnalysisJobRepository extends JpaRepository<AnalysisJob, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select j from AnalysisJob j where j.id = :id")
    Optional<AnalysisJob> findLocked(@Param("id") String id);
    @Query("select j.id from AnalysisJob j where ((j.status = 'QUEUED' and j.updatedAt = j.createdAt) or (j.status in ('QUEUED', 'PROCESSING') and j.updatedAt < :before)) order by j.createdAt")
    java.util.List<String> findPendingIds(@Param("before") java.time.Instant before, org.springframework.data.domain.Pageable page);
    Optional<AnalysisJob> findByIdAndUserId(String id, Long userId);
}
