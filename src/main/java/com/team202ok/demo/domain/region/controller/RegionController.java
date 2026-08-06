package com.team202ok.demo.domain.region.controller;

import com.team202ok.demo.domain.region.dto.RegionRes;
import com.team202ok.demo.domain.region.service.RegionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/regions")
@RequiredArgsConstructor
public class RegionController {
    private final RegionService regionService;

    @GetMapping
    public List<RegionRes> getRegions(
            @RequestParam(required = false) String sido,
            @RequestParam(required = false) String gugun
    ) {
        return regionService.getRegions(sido, gugun);
    }
}
