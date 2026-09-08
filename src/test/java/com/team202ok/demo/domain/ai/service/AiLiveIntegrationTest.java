package com.team202ok.demo.domain.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import java.nio.file.*;
import static org.assertj.core.api.Assertions.*;

@EnabledIfEnvironmentVariable(named = "RUN_AI_LIVE", matches = "true")
class AiLiveIntegrationTest {
    @Test void parsesActualAiResponse() throws Exception {
        String url = System.getenv("AI_SERVER_URL");
        byte[] bytes = Files.readAllBytes(Path.of(System.getenv("AI_TEST_IMAGE")));
        var client = new AiModelClient(WebClient.builder().baseUrl(url).build(), new ObjectMapper());
        long start = System.nanoTime();
        var response = client.requestAnalysis(new MockMultipartFile("image", "test.png", "image/png", bytes));
        response.validate();
        assertThat(response.getRawJson()).isNotBlank();
        assertThat(response.getObjects()).isNotNull();
        System.out.printf("LIVE_AI seconds=%.3f objects=%d additionalObjects=%d%n", (System.nanoTime()-start)/1e9,
                response.getObjects().size(), response.getAdditionalObjects().size());
    }
}
