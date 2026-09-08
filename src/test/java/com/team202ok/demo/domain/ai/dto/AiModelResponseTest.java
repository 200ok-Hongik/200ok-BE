package com.team202ok.demo.domain.ai.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class AiModelResponseTest {
    private final ObjectMapper mapper = new ObjectMapper();

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
