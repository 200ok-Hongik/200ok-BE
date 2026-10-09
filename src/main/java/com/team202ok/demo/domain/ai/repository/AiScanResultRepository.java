package com.team202ok.demo.domain.ai.repository;

import com.team202ok.demo.domain.ai.entity.AiScanResult;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface AiScanResultRepository extends JpaRepository<AiScanResult, Long> {
    Optional<AiScanResult> findFirstByScanResultIdOrderByCreatedAtDesc(Long scanResultId);
    List<AiScanResult> findByScanResultIdOrderByCreatedAtAsc(Long scanResultId);
    Optional<AiScanResult> findByScanResultIdAndObjectId(Long scanResultId, String objectId);
}
