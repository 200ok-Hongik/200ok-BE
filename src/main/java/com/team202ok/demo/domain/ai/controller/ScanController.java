package com.team202ok.demo.domain.ai.controller;

import com.team202ok.demo.domain.ai.dto.AiRes;
import com.team202ok.demo.domain.ai.dto.AiReq;
import com.team202ok.demo.domain.ai.service.AiAnalysisService;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/scans")
@RequiredArgsConstructor
@Tag(name = "AI 스캔", description = "이미지 분석, 결과 수정·확정, 최종 분리배출 안내를 제공합니다.")
public class ScanController {
    private final AiAnalysisService aiAnalysisService;

    @GetMapping("/{scanResultId}/objects")
    @Operation(summary = "스캔 객체 목록 조회", description = "다중 객체와 추가 후보를 포함한 AI 분석 결과를 조회합니다.")
    public AiRes.Analyze getScanObjects(@PathVariable Long scanResultId,
                                        @Parameter(hidden = true) @AuthenticationPrincipal Long userId) {
        return aiAnalysisService.getScanObjects(scanResultId, userId);
    }

    @GetMapping("/{scanResultId}/objects/{objectId}")
    @Operation(summary = "스캔 객체 상세 조회", description = "objectId로 특정 AI 분석 객체를 조회합니다.")
    public AiRes.ScanDetail getScanObject(@PathVariable Long scanResultId, @PathVariable String objectId,
                                          @Parameter(hidden = true) @AuthenticationPrincipal Long userId) {
        return aiAnalysisService.getScanObject(scanResultId, objectId, userId);
    }

    @PatchMapping("/{scanResultId}/objects/{objectId}/result")
    @Operation(summary = "객체별 분석 결과 수정 및 확정")
    public AiRes.ConfirmedResult updateObjectResult(@PathVariable Long scanResultId, @PathVariable String objectId,
                                                     @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
                                                     @RequestBody AiReq.UpdateResult request) {
        return aiAnalysisService.updateObjectResult(scanResultId, objectId, request, userId);
    }

    @GetMapping("/{scanResultId}/objects/{objectId}/disposal-guide")
    @Operation(summary = "객체별 최종 분리배출 안내 조회")
    public AiRes.DisposalGuideDetail getObjectDisposalGuide(@PathVariable Long scanResultId, @PathVariable String objectId,
                                                             @Parameter(hidden = true) @AuthenticationPrincipal Long userId) {
        return aiAnalysisService.getObjectDisposalGuide(scanResultId, objectId, userId);
    }

}
