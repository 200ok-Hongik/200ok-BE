package com.team202ok.demo.domain.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(OutputCaptureExtension.class)
class AiDiagnosticLoggingTest {
    @Test void logsRawResponseAndAllInvalidFieldsWithJobId(CapturedOutput output) {
        String raw = "{\"objects\":[{\"objectId\":\"one\",\"finalResult\":{\"states\":{}}}],\"additionalObjects\":[]}";
        var web = WebClient.builder().exchangeFunction(request -> Mono.just(
                ClientResponse.create(HttpStatus.OK).header("Content-Type", "application/json").body(raw).build())).build();
        var client = new AiModelClient(web, new ObjectMapper());
        try (var context = org.slf4j.MDC.putCloseable("aiJobId", "diagnostic-job")) {
            assertThatThrownBy(() -> client.requestAnalysis(new MockMultipartFile("image", "test.png", "image/png", new byte[]{1})))
                    .hasMessageContaining("V1_VALIDATION")
                    .hasMessageContaining("objects[0].bbox")
                    .hasMessageContaining("objects[0].finalResult.itemCode")
                    .hasMessageContaining("objects[0].finalResult.source");
        }
        assertThat(output.getAll()).contains("[AI RAW] trace=diagnostic-job", "jsonEscaped=", "V1_VALIDATION");
    }

    @Test void distinguishesMalformedJsonFromConnectionFailure(CapturedOutput output) {
        var web = WebClient.builder().exchangeFunction(request -> Mono.just(
                ClientResponse.create(HttpStatus.OK).body("not-json\nsecond-line").build())).build();
        var client = new AiModelClient(web, new ObjectMapper());
        assertThatThrownBy(() -> client.requestAnalysis(new MockMultipartFile("image", "test.png", "image/png", new byte[]{1})))
                .hasMessageContaining("JSON_PARSE");
        assertThat(output.getAll()).contains("not-json\\nsecond-line");
    }
}
