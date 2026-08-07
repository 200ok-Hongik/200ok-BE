package com.team202ok.demo.domain.disposal.repository;

import com.team202ok.demo.domain.disposal.entity.DisposalDecision;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DisposalDecisionRepository extends JpaRepository<DisposalDecision, Long> {
    Optional<DisposalDecision> findFirstByScanResultIdOrderByCreatedAtDesc(Long scanResultId);
}
