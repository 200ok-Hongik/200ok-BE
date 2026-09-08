package com.team202ok.demo.domain.ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;
import java.util.HashSet;

/** AI /analyze V1. Unknown analysis metadata remains in the stored raw response. */
@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AiModelResponse {
    private List<DetectedObject> objects;
    // Candidate schema has not been agreed yet; preserve it without guessing fields.
    private List<JsonNode> additionalObjects;
    @JsonIgnore
    private String rawJson;

    public void setRawJson(String rawJson) { this.rawJson = rawJson; }

    public void validate() {
        if (objects == null || additionalObjects == null) {
            throw new IllegalArgumentException("AI V1 requires objects and additionalObjects arrays");
        }
        var ids = new HashSet<String>();
        for (DetectedObject object : objects) {
            if (object == null || object.objectId() == null || object.objectId().isBlank()
                    || !ids.add(object.objectId()) || object.bbox() == null
                    || !object.bbox().valid() || object.finalResult() == null
                    || object.finalResult().itemCode() == null || object.finalResult().itemCode().isBlank()
                    || object.finalResult().states() == null
                    || object.finalResult().source() == null || object.finalResult().source().isBlank()) {
                throw new IllegalArgumentException("Invalid AI V1 object");
            }
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DetectedObject(String objectId, Bbox bbox, FinalResult finalResult) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Bbox(Double xMin, Double yMin, Double xMax, Double yMax) {
        public boolean valid() {
            return xMin != null && yMin != null && xMax != null && yMax != null
                    && Double.isFinite(xMin) && Double.isFinite(yMin)
                    && Double.isFinite(xMax) && Double.isFinite(yMax)
                    && xMin >= 0 && yMin >= 0 && xMax > xMin && yMax > yMin;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FinalResult(String itemCode, Map<String, JsonNode> states, String source) {}
}
