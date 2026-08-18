package com.team202ok.demo.domain.analysis.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AiResJsonTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void analyzeUsesScanResultId() throws Exception {
        AiRes.Analyze response = new AiRes.Analyze(1L, "PET", new BigDecimal("0.95"), "v1", List.of());

        JsonNode json = objectMapper.valueToTree(response);

        assertThat(json.has("scanResultId")).isTrue();
        assertThat(json.has("scanId")).isFalse();
    }

    @Test
    void scanDetailSeparatesAiAndConfirmedResults() {
        AiRes.ScanDetail.Category aiCategory = new AiRes.ScanDetail.Category(
                1L, "PET", "페트병", new BigDecimal("0.95"), "AI");
        AiRes.ScanDetail response = new AiRes.ScanDetail(
                1L, "https://example.com/image.jpg", aiCategory, List.of(), null, LocalDateTime.now());

        JsonNode json = objectMapper.valueToTree(response);

        assertThat(json.has("aiCategory")).isTrue();
        assertThat(json.has("aiStates")).isTrue();
        assertThat(json.has("confirmedResult")).isTrue();
        assertThat(json.has("category")).isFalse();
        assertThat(json.has("states")).isFalse();
        assertThat(json.has("userResult")).isFalse();
        assertThat(json.path("aiCategory").has("categorySource")).isTrue();
    }
}
