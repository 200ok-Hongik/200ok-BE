package com.team202ok.demo.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record TrendAnalysisResult(
        @JsonProperty("is_trend") boolean trend,
        @JsonProperty("canonical_menu_names") List<String> canonicalMenuNames,
        @JsonProperty("menu_weights") List<MenuWeight> menuWeights,
        @JsonProperty("confidence_score") double confidenceScore,
        String summary
) {
    public TrendAnalysisResult {
        canonicalMenuNames = canonicalMenuNames == null ? List.of() : List.copyOf(canonicalMenuNames);
        menuWeights = menuWeights == null ? List.of() : List.copyOf(menuWeights);
    }

    public record MenuWeight(
            @JsonProperty("menu_id") Long menuId,
            String name,
            float weight
    ) {
    }
}
