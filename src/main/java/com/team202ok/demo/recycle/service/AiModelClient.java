package com.team202ok.demo.recycle.service;

import com.team202ok.demo.recycle.dto.AiModelResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiModelClient {

    private final WebClient aiServerWebClient;

    // true면 목 데이터 반환, false면 실제 AI 서버 호출 (기본값 true)
    @Value("${ai.server.use-mock:true}")
    private boolean useMock;

    public AiModelResponse requestAnalysis(String imageUrl) {
        if (useMock) {
            log.info("[AI Client] Using Mock Response for image: {}", imageUrl);
            return getMockResponse();
        }

        try {
            log.info("[AI Client] Requesting real AI server for image: {}", imageUrl);

            // 실제 Python AI 서버로 전송할 요청 바디
            Map<String, String> requestBody = Map.of("imageUrl", imageUrl);

            return aiServerWebClient.post()
                    .uri("/analyze") // AI 서버의 실제 엔드포인트 경로에 맞게 수정하세요
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(AiModelResponse.class)
                    .block();

        } catch (Exception e) {
            log.error("[AI Client] Failed to communicate with AI server, fallback to mock. Error: {}", e.getMessage());
            return getMockResponse();
        }
    }

    private AiModelResponse getMockResponse() {
        return AiModelResponse.builder()
                .categoryCode("PET_BOTTLE")
                .categoryConfidence(new BigDecimal("0.97"))
                .modelVersion("v0.0.0-mock")
                .rawJson("{}")
                .checkItems(List.of(
                        AiModelResponse.CheckItem.builder()
                                .checkItemName("isTransparent")
                                .statusValue("true")
                                .confidence(new BigDecimal("0.95"))
                                .build(),
                        AiModelResponse.CheckItem.builder()
                                .checkItemName("hasLabel")
                                .statusValue("true")
                                .confidence(new BigDecimal("0.91"))
                                .build(),
                        AiModelResponse.CheckItem.builder()
                                .checkItemName("hasCap")
                                .statusValue("true")
                                .confidence(new BigDecimal("0.98"))
                                .build()
                ))
                .build();
    }

    public boolean checkServerHealth() {
        try {
            log.info("[AI Client] Checking AI server health");

            String response = aiServerWebClient.get()
                    .uri("/health")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("[AI Client] AI server health check success: {}", response);
            return true;

        } catch (Exception e) {
            log.error("[AI Client] AI server health check failed: {}", e.getMessage());
            return false;
        }
    }
}