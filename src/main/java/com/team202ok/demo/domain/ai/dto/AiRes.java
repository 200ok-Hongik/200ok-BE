package com.team202ok.demo.domain.ai.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

public final class AiRes {

    private AiRes() {
    }

    /**
     * 프론트엔드(클라이언트)로 최종 나가는 분석 응답 DTO
     */
    @Builder
    public record Analyze(
            Long scanResultId,
            List<AiModelResponse.DetectedObject> objects,
            List<com.fasterxml.jackson.databind.JsonNode> additionalObjects
    ) {
        @Builder
        public record ChecklistResult(
                Long checklistId,
                String checkItemName,
                String statusValue,
                BigDecimal confidence
        ) {}
    }

    @Builder
    public record FinalGuide(
            Long scanResultId,
            Long decisionId,
            boolean isPass,
            String categoryName,
            String guideMessage,
            List<String> steps,
            ScheduleInfo schedule
    ) {
        @Builder
        public record ScheduleInfo(
                String dischargeDays,
                String dischargeTime
        ) {}
    }

    @Builder
    public record ScanDetail(Long scanResultId, String imageUrl, Category aiCategory,
                             List<Analyze.ChecklistResult> aiStates, UserResult confirmedResult,
                             java.time.LocalDateTime createdAt) {
        @Builder public record Category(Long categoryId, String code, String name, BigDecimal confidence, String categorySource) {}
        @Builder public record UserResult(Long decisionId, Category category, boolean isPass,
                                          List<Analyze.ChecklistResult> states) {}
    }

    @Builder
    public record ConfirmedResult(Long scanResultId, ScanDetail.Category category,
                                  List<Analyze.ChecklistResult> states, boolean isConfirmed, Long decisionId) {}

    @Builder
    public record DisposalGuideDetail(Long decisionId, Long scanResultId, ScanDetail.Category category,
                                      boolean isPass, String guideMessage, String cautionMessage,
                                      List<GuideCheckItem> checkItems, FinalGuide.ScheduleInfo schedule,
                                      String finalGuideMessage) {
        @Builder public record GuideCheckItem(Long checklistId, String checkItemName, String statusValue,
                                              String guideMessage, boolean isSatisfied) {}
    }
}
