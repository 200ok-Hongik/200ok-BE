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
        log.info("[AI Client] === AI 분석 요청 시작 ===");
        log.info("[AI Client] 입력된 이미지 URL: {}", imageUrl);
        log.info("[AI Client] 현재 Mock 모드 여부 (useMock): {}", useMock);

        if (useMock) {
            log.warn("[AI Client] [Mock 모드 작동] 실제 AI 서버를 호출하지 않고 목(Mock) 데이터를 반환합니다.");
            AiModelResponse mockResponse = getMockResponse();
            log.info("[AI Client] [Mock 데이터 반환 완료] CategoryCode: {}, ModelVersion: {}",
                    mockResponse.getCategoryCode(), mockResponse.getModelVersion());
            return mockResponse;
        }

        try {
            log.info("[AI Client] [실제 AI 서버 호출 시도] 대상 URI: /analyze");

            // 실제 Python AI 서버로 전송할 요청 바디
            Map<String, String> requestBody = Map.of("imageUrl", imageUrl);
            log.debug("[AI Client] 전송할 Request Body: {}", requestBody);

            AiModelResponse response = aiServerWebClient.post()
                    .uri("/analyze") // AI 서버의 실제 엔드포인트 경로에 맞게 수정하세요
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(AiModelResponse.class)
                    .block();

            log.info("[AI Client] [실제 AI 서버 호출 성공] 응답 데이터 수신 완료. CategoryCode: {}",
                    response != null ? response.getCategoryCode() : "null");
            return response;

        } catch (Exception e) {
            log.error("[AI Client] [실제 AI 서버 통신 실패] AI 서버와 통신 중 에러가 발생했습니다. 에러 메시지: {}", e.getMessage(), e);
            log.warn("[AI Client] [Fallback 작동] 통신 실패로 인해 목(Mock) 데이터로 대체 반환합니다.");

            AiModelResponse fallbackResponse = getMockResponse();
            log.info("[AI Client] [Fallback 목 데이터 반환 완료]");
            return fallbackResponse;
        }
    }

    private AiModelResponse getMockResponse() {
        log.debug("[AI Client] getMockResponse() 호출됨 - 목 데이터 생성 중");
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
        log.info("[AI Client] === AI 서버 헬스체크 시작 ===");
        try {
            log.info("[AI Client] [헬스체크 요청] 대상 URI: /health");

            String response = aiServerWebClient.get()
                    .uri("/health")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("[AI Client] [헬스체크 성공] 서버 응답: {}", response);
            return true;

        } catch (Exception e) {
            log.error("[AI Client] [헬스체크 실패] AI 서버 상태 확인 불가. 에러 메시지: {}", e.getMessage(), e);
            return false;
        }
    }
}