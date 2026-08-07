package com.team202ok.demo.domain.analysis.repository;

import com.team202ok.demo.domain.analysis.entity.AiScanDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AiScanDetailRepository extends JpaRepository<AiScanDetail, Long> {
    List<AiScanDetail> findByAiScanResultId(Long aiScanResultId);
}
