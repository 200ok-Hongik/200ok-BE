package com.team202ok.demo.domain.region.controller;

import com.team202ok.demo.domain.region.dto.RegionRes;
import com.team202ok.demo.domain.region.service.RegionService;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/regions")
@RequiredArgsConstructor
@Tag(name = "지역", description = "기본 배출 지역 설정에 사용할 지역 목록을 제공합니다.")
public class RegionController {
    private final RegionService regionService;

    @GetMapping
    @Operation(summary = "지역 목록 조회", description = "시·도와 구·군 조건으로 선택 가능한 지역 목록을 조회합니다. 조건을 생략하면 전체 목록을 반환합니다.")
    public List<RegionRes> getRegions(
            @Parameter(description = "시·도", example = "서울특별시") @RequestParam(required = false) String sido,
            @Parameter(description = "구·군", example = "마포구") @RequestParam(required = false) String gugun
    ) {
        return regionService.getRegions(sido, gugun);
    }
}
