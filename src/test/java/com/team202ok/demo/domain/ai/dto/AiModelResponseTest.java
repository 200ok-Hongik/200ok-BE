package com.team202ok.demo.domain.ai.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class AiModelResponseTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void acceptsLivePendingReviewResponseWithoutLosingTypedStates() throws Exception {
        // Actual /analyze response for the supplied JPEG, observed 2026-09-20.
        var response = mapper.readValue("""
                {"image":{"imageId":null,"width":4031,"height":2265},
                 "imageQuality":{"status":"SUFFICIENT","issues":[]},
                 "analysis":{"mode":"ORIGINAL_NUMBERED_BBOX","modelVersion":"v1.1.0","currentLevel":"LOW",
                   "attemptedLevels":["LOW"],"needsHighAnalysis":false,"highTriggerReasons":[]},
                 "objects":[{"objectId":"object_1","bbox":{"xMin":1319,"yMin":700,"xMax":2756,"yMax":1236},
                   "yolo":{"itemCode":"CLEAR_PET_BOTTLE","className":"pet_bottle_clear","confidence":0.906},
                   "vlm":{"itemCode":"CLEAR_PET_BOTTLE","confidence":0.92,
                     "states":{"isTransparent":true,"hasLabel":true,"isEmpty":true,"hasCap":true,"isContaminated":false,"isCrushed":false},
                     "yoloItemMatches":true,"multipleObjectsInBox":false,"hasUnlabeledNearbyObject":false,
                     "isTooSmallOrBlurred":false,"needsReview":false,"reviewReasons":[]},
                   "review":{"status":"PENDING"},"finalResult":null}],"additionalObjects":[]}
                """, AiModelResponse.class);
        response.validate();
        var object = response.getObjects().get(0);
        assertThat(object.finalResult().itemCode()).isEqualTo("CLEAR_PET_BOTTLE");
        assertThat(object.finalResult().source()).isEqualTo("VLM");
        assertThat(object.finalResult().states().get("hasLabel").booleanValue()).isTrue();
        assertThat(object.finalResult().states().get("isContaminated").booleanValue()).isFalse();
        assertThat(object.review().get("status").asText()).isEqualTo("PENDING");
        var roundTrip = mapper.readValue(mapper.writeValueAsString(response), AiModelResponse.class);
        roundTrip.validate();
        assertThat(roundTrip.getObjects().get(0)).isEqualTo(object);
    }

    @Test
    void explicitFinalResultWinsOverVlmAndMissingResultsStillFail() throws Exception {
        String raw = """
                {"objects":[{"objectId":"one","bbox":{"xMin":0,"yMin":0,"xMax":10,"yMax":10},
                 "finalResult":{"itemCode":"CAN","states":{},"source":"USER"},
                 "vlm":{"itemCode":"PET_BOTTLE","states":{}}}],"additionalObjects":[]}
                """;
        var response = mapper.readValue(raw, AiModelResponse.class);
        response.validate();
        assertThat(response.getObjects().get(0).finalResult().itemCode()).isEqualTo("CAN");
        var missing = mapper.readValue("""
                {"objects":[{"objectId":"one","bbox":{"xMin":0,"yMin":0,"xMax":10,"yMax":10},
                  "finalResult":null}],"additionalObjects":[]}
                """, AiModelResponse.class);
        assertThatIllegalArgumentException().isThrownBy(missing::validate)
                .withMessageContaining("finalResult: missing/null");
    }

    @Test
    void preservesMultipleObjectsTypedStatesAndUnknownCandidates() throws Exception {
        String object = """
                {"objectId":"object_1","bbox":{"xMin":320,"yMin":540,"xMax":640,"yMax":800},
                 "finalResult":{"itemCode":"PET_BOTTLE","states":{"hasLabel":true,"dirty":false,"unknown":null},"source":"VLM_LOW"},
                 "lowAnalysis":{"extra":"metadata"}}
                """;
        AiModelResponse response = mapper.readValue("{\"objects\":[" + object + ","
                + object.replace("object_1", "object_2") + "],\"additionalObjects\":[{\"candidate\":1}]}", AiModelResponse.class);
        response.validate();
        assertThat(response.getObjects()).hasSize(2);
        var states = response.getObjects().get(0).finalResult().states();
        assertThat(states.get("hasLabel").isBoolean()).isTrue();
        assertThat(states.get("dirty").booleanValue()).isFalse();
        assertThat(states.get("unknown").isNull()).isTrue();
        assertThat(response.getAdditionalObjects().get(0).get("candidate").intValue()).isEqualTo(1);
    }

    @Test
    void allowsNoDetectionsButRejectsOldContract() throws Exception {
        mapper.readValue("{\"objects\":[],\"additionalObjects\":[]}", AiModelResponse.class).validate();
        var old = mapper.readValue("{\"categoryCode\":\"PET_BOTTLE\"}", AiModelResponse.class);
        assertThatIllegalArgumentException().isThrownBy(old::validate);
    }

    @Test
    void rejectsDuplicateIdsAndIncompleteBbox() throws Exception {
        String object = """
                {"objectId":"object_1","bbox":{"xMin":0,"yMin":0,"xMax":100,"yMax":100},
                 "finalResult":{"itemCode":"PET_BOTTLE","states":{},"source":"VLM_LOW"}}
                """;
        var duplicate = mapper.readValue("{\"objects\":[" + object + "," + object + "],\"additionalObjects\":[]}", AiModelResponse.class);
        assertThatIllegalArgumentException().isThrownBy(duplicate::validate);
        var incomplete = mapper.readValue("{\"objects\":[" + object.replace("\"xMax\":100,", "") + "],\"additionalObjects\":[]}", AiModelResponse.class);
        assertThatIllegalArgumentException().isThrownBy(incomplete::validate);
    }
}
