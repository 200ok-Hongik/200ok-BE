package com.team202ok.demo.domain.analysis.repository;

import com.team202ok.demo.domain.analysis.entity.AiScanResult;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AiScanResultRepository extends JpaRepository<AiScanResult, Long> {
    Optional<AiScanResult> findFirstByScanResultIdOrderByCreatedAtDesc(Long scanResultId);
}
