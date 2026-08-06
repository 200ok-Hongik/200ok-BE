package com.team202ok.demo.domain.region.service;

import com.team202ok.demo.domain.region.dto.RegionRes;
import com.team202ok.demo.domain.region.entity.Region;
import com.team202ok.demo.domain.region.repository.RegionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RegionServiceImpl implements RegionService {
    private final RegionRepository regionRepository;

    @Override
    public List<RegionRes> getRegions(String sido, String gugun) {
        return regionRepository.findAll().stream()
                .filter(region -> sido == null || sido.equals(region.getSido()))
                .filter(region -> gugun == null || gugun.equals(region.getGugun()))
                .map(this::toResponse)
                .toList();
    }

    private RegionRes toResponse(Region region) {
        return new RegionRes(region.getId(), region.getRegionCode(), region.getSido(), region.getGugun(), region.getDong());
    }
}
