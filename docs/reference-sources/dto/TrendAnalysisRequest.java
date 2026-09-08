package com.team202ok.demo.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record TrendAnalysisRequest(
        Keyword keyword,
        List<Source> sources,
        @JsonProperty("menu_candidates") List<MenuCandidate> menuCandidates
) {
    public TrendAnalysisRequest {
        sources = sources == null ? List.of() : List.copyOf(sources);
        menuCandidates = menuCandidates == null ? List.of() : List.copyOf(menuCandidates);
    }

    public record Keyword(Long id, String text) {
    }

    public record Source(String type, String text) {
    }

    public record MenuCandidate(
            @JsonProperty("menu_id") Long menuId,
            String name
    ) {
    }
}
