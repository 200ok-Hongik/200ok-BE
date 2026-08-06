package com.team202ok.demo.domain.analysis.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Getter
@NoArgsConstructor
public class AiModelResponse {

    private String categoryCode;
    private BigDecimal categoryConfidence;
    private String modelVersion;
    private String rawJson; // 원본 응답 전체(감사/디버깅용, ai_scan_results.raw_response에 저장)
    private List<CheckItem> checkItems;

    @Builder
    public AiModelResponse(String categoryCode, BigDecimal categoryConfidence,
                           String modelVersion, String rawJson, List<CheckItem> checkItems) {
        this.categoryCode = categoryCode;
        this.categoryConfidence = categoryConfidence;
        this.modelVersion = modelVersion;
        this.rawJson = rawJson;
        this.checkItems = checkItems;
    }

    @Getter
    @NoArgsConstructor
    public static class CheckItem {
        private String checkItemName;
        private String statusValue;
        private BigDecimal confidence;

        @Builder
        public CheckItem(String checkItemName, String statusValue, BigDecimal confidence) {
            this.checkItemName = checkItemName;
            this.statusValue = statusValue;
            this.confidence = confidence;
        }
    }

    public CheckItem getCheckItemByName(String name) {
        return checkItems.stream()
                .filter(c -> c.getCheckItemName().equals(name))
                .findFirst()
                .orElse(null);
    }
}