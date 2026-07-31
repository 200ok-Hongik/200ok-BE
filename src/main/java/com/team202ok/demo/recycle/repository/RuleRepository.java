package com.team202ok.demo.recycle.repository;

import com.team202ok.demo.recycle.domain.RecycleRule;
import com.team202ok.demo.recycle.domain.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RuleRepository extends JpaRepository<RecycleRule, Long> {
    List<RecycleRule> findByItemCodeAndRegion_RegionCode(String itemCode, String regionCode);

    List<RecycleRule> findByItemCode(String itemCode);

    Optional<RecycleRule> findByRuleId(String ruleId);
}
