package com.team202ok.demo.domain.ai.repository;

import com.team202ok.demo.domain.ai.entity.ScanResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ScanResultRepository extends JpaRepository<ScanResult, Long> {
    List<ScanResult> findByUserIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThanOrderByCreatedAtDesc(
            Long userId, LocalDateTime start, LocalDateTime end);
}
