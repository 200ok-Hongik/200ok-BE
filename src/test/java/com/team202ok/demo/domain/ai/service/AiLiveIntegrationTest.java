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
        Path image = Path.of(System.getenv("AI_TEST_IMAGE"));
        byte[] bytes = Files.readAllBytes(image);
        String contentType = Files.probeContentType(image);
        assertThat(contentType).as("Test file must be an image").startsWith("image/");
        var client = new AiModelClient(WebClient.builder().baseUrl(url).build(), new ObjectMapper());
        long start = System.nanoTime();
        System.out.printf("LIVE_AI url=%s image=%s contentType=%s bytes=%d%n",
                url, image.getFileName(), contentType, bytes.length);
        try (var context = org.slf4j.MDC.putCloseable("aiJobId", "local-live-diagnostic")) {
            var response = client.requestAnalysis(new MockMultipartFile("image", image.getFileName().toString(), contentType, bytes));
            response.validate();
            assertThat(response.getRawJson()).isNotBlank();
            assertThat(response.getObjects()).isNotNull();
            System.out.printf("LIVE_AI seconds=%.3f objects=%d additionalObjects=%d%n", (System.nanoTime()-start)/1e9,
                    response.getObjects().size(), response.getAdditionalObjects().size());
        } finally {
            System.out.printf("LIVE_AI totalSeconds=%.3f%n", (System.nanoTime()-start)/1e9);
        }
    }
}
