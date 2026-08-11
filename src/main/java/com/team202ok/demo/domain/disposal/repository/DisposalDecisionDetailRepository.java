package com.team202ok.demo.domain.disposal.repository;

import com.team202ok.demo.domain.disposal.entity.DisposalDecisionDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DisposalDecisionDetailRepository extends JpaRepository<DisposalDecisionDetail, Long> {
    List<DisposalDecisionDetail> findByDisposalDecisionId(Long disposalDecisionId);
}
