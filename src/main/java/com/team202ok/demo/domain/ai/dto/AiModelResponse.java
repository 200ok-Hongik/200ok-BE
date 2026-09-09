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
        var errors = new java.util.ArrayList<String>();
        if (objects == null) errors.add("objects: required array is missing/null");
        if (additionalObjects == null) errors.add("additionalObjects: required array is missing/null");
        var ids = new HashSet<String>();
        if (objects != null) for (int i = 0; i < objects.size(); i++) {
            var object = objects.get(i);
            String path = "objects[" + i + "]";
            if (object == null) { errors.add(path + ": null object"); continue; }
            if (object.objectId() == null || object.objectId().isBlank()) errors.add(path + ".objectId: missing/blank");
            else if (!ids.add(object.objectId())) errors.add(path + ".objectId: duplicate");
            if (object.bbox() == null) errors.add(path + ".bbox: missing/null");
            else if (!object.bbox().valid()) errors.add(path + ".bbox: requires finite coordinates, xMin/yMin >= 0, xMax > xMin, yMax > yMin; actual=" + object.bbox());
            if (object.finalResult() == null) { errors.add(path + ".finalResult: missing/null"); continue; }
            var result = object.finalResult();
            if (result.itemCode() == null || result.itemCode().isBlank()) errors.add(path + ".finalResult.itemCode: missing/blank");
            if (result.states() == null) errors.add(path + ".finalResult.states: missing/null");
            if (result.source() == null || result.source().isBlank()) errors.add(path + ".finalResult.source: missing/blank");
        }
        if (!errors.isEmpty()) throw new IllegalArgumentException("Invalid AI V1 response: " + String.join("; ", errors));
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
