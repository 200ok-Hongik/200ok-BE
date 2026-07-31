package com.team202ok.demo.recycle.repository;

import com.team202ok.demo.recycle.entity.RegionSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RegionScheduleRepository extends JpaRepository<RegionSchedule, Long> {

//    List<RegionSchedule> findByRegionId(Long regionId);
    Optional<RegionSchedule> findByRegionCode(String regionCode);
}