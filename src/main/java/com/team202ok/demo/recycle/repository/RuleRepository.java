package com.team202ok.demo.recycle.repository;

import com.team202ok.demo.recycle.domain.RecycleRule;
import com.team202ok.demo.recycle.domain.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RuleRepository extends JpaRepository<RecycleRule, Long> {
    List<RecycleRule> findByItemAndRegion_Region(String item, String region);
}