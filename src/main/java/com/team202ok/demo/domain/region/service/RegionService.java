package com.team202ok.demo.domain.region.service;

import com.team202ok.demo.domain.region.dto.RegionRes;

import java.util.List;

public interface RegionService {
    List<RegionRes> getRegions(String sido, String gugun);
}
