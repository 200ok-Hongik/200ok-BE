package com.team202ok.demo.domain.analysis.repository;

import com.team202ok.demo.domain.analysis.entity.ScanResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScanResultRepository extends JpaRepository<ScanResult, Long> {
}
