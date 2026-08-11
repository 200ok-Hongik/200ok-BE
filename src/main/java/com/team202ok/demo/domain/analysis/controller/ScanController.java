package com.team202ok.demo.domain.analysis.controller;

import com.team202ok.demo.domain.analysis.dto.AiRes;
import com.team202ok.demo.domain.analysis.dto.AiReq;
import com.team202ok.demo.domain.analysis.service.AiAnalysisService;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/scans")
@RequiredArgsConstructor
@Tag(name = "AI 스캔", description = "이미지 분석, 결과 수정·확정, 최종 분리배출 안내를 제공합니다.")
public class ScanController {
    private final AiAnalysisService aiAnalysisService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "분리수거 이미지 분석", description = "이미지를 AI 서버로 전송하고 AI 품목·상태 분석 결과를 저장합니다.")
    public ResponseEntity<AiRes.Analyze> analyze(@RequestPart("image") MultipartFile image,
                                                 @Parameter(description = "임시 사용자 ID (카카오 로그인 연동 전)", example = "1") @RequestParam Long userId) {
        return ResponseEntity.status(201).body(aiAnalysisService.analyze(image, userId));
    }

    @GetMapping("/{scanResultId}")
    @Operation(summary = "분석 결과 조회", description = "저장된 AI 분석 결과와 사용자가 확정한 결과를 조회합니다.")
    public AiRes.ScanDetail getScan(@Parameter(description = "스캔 결과 ID", example = "1") @PathVariable Long scanResultId,
                                    @Parameter(description = "임시 사용자 ID", example = "1") @RequestParam Long userId) {
        return aiAnalysisService.getScan(scanResultId, userId);
    }

    @PatchMapping("/{scanResultId}/result")
    @Operation(summary = "분석 결과 수정 및 확정", description = "품목과 체크 상태를 수정하여 최종 분리배출 판단을 저장합니다.")
    public AiRes.ConfirmedResult updateResult(@Parameter(description = "스캔 결과 ID", example = "1") @PathVariable Long scanResultId,
                                               @Parameter(description = "임시 사용자 ID", example = "1") @RequestParam Long userId,
                                               @RequestBody AiReq.UpdateResult request) {
        return aiAnalysisService.updateResult(scanResultId, request, userId);
    }

    @GetMapping("/{scanResultId}/disposal-guide")
    @Operation(summary = "최종 분리배출 안내 조회", description = "확정된 품목·상태, 품목별 가이드, 사용자 지역 배출 일정을 결합해 반환합니다.")
    public AiRes.DisposalGuideDetail getDisposalGuide(@Parameter(description = "스캔 결과 ID", example = "1") @PathVariable Long scanResultId,
                                                       @Parameter(description = "임시 사용자 ID", example = "1") @RequestParam Long userId) {
        return aiAnalysisService.getDisposalGuide(scanResultId, userId);
    }

    @PostMapping({"/{scanResultId}/comments", "/{scanResultId}/feedback"})
    @Operation(summary = "AI 분석 의견 등록", description = "AI 분석 결과에 대한 자유 의견을 저장합니다.")
    public ResponseEntity<Void> createFeedback(@Parameter(description = "스캔 결과 ID", example = "1") @PathVariable Long scanResultId,
                               @Parameter(description = "임시 사용자 ID", example = "1") @RequestParam Long userId,
                               @RequestBody FeedbackRequest request) {
        aiAnalysisService.createComment(scanResultId, request.comment(), userId);
        return ResponseEntity.noContent().build();
    }

    public record FeedbackRequest(String comment) {
    }
}
