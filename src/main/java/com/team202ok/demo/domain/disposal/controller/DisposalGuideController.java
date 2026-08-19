package com.team202ok.demo.domain.disposal.controller;

import com.team202ok.demo.domain.disposal.dto.DisposalGuideRes;
import com.team202ok.demo.domain.disposal.service.DisposalGuideService;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/trash-categories")
@RequiredArgsConstructor
@Tag(name = "분리배출 가이드", description = "품목 목록과 품목별 기본 분리배출 가이드를 제공합니다.")
public class DisposalGuideController {
    private final DisposalGuideService disposalGuideService;

    @GetMapping
    @Operation(summary = "쓰레기 품목 목록 조회", description = "가이드 화면에서 선택할 수 있는 쓰레기 품목 목록을 조회합니다.")
    public List<DisposalGuideRes.Category> getCategories() { return disposalGuideService.getCategories(); }

    @GetMapping("/{categoryId}/guide")
    @Operation(summary = "품목별 가이드 조회", description = "품목의 배출 방법, 주의사항, 체크리스트를 조회합니다.")
    public DisposalGuideRes.Detail getGuide(@Parameter(description = "쓰레기 품목 ID", example = "1") @PathVariable Long categoryId) { return disposalGuideService.getGuide(categoryId); }
}
