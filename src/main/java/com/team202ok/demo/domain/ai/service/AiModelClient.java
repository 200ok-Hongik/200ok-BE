package com.team202ok.demo.domain.ai.service;

import com.team202ok.demo.domain.ai.dto.AiModelResponse;
import com.team202ok.demo.global.exception.code.GeneralErrorCode;
import com.team202ok.demo.global.exception.custom.ProjectException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiModelClient {

    private static final Duration ANALYSIS_TIMEOUT = Duration.ofSeconds(120);

    private final WebClient aiServerWebClient;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    public AiModelResponse requestAnalysis(MultipartFile image) {
        long started = System.nanoTime();
        log.info("[AI Client] === AI 분석 요청 시작 ===");
        log.info("[AI Client] 입력된 이미지 파일: {}", image.getOriginalFilename());

        try {
            log.info("[AI Client] [실제 AI 서버 호출 시도] 대상 URI: /analyze");

            MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
            bodyBuilder.part("image", image.getResource())
                    .filename(image.getOriginalFilename());
            log.info("[AI Client] 전송 형식: multipart/form-data, imageName={}, imageSize={} bytes",
                    image.getOriginalFilename(), image.getSize());

            String rawResponse = aiServerWebClient.post()
                    .uri("/analyze")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(ANALYSIS_TIMEOUT)
                    .block();

            AiModelResponse response = objectMapper.readValue(rawResponse, AiModelResponse.class);
            response.validate();
            response.setRawJson(rawResponse);
            log.info("[AI Client] 분석 완료. 객체 수: {}, elapsedMs={}", response.getObjects().size(),
                    (System.nanoTime() - started) / 1_000_000);
            return response;

        } catch (WebClientResponseException e) {
            log.error("[AI Client] AI 서버 오류: status={}, contentType={}, body={}",
                    e.getStatusCode(), e.getHeaders().getContentType(), e.getResponseBodyAsString(), e);
            throw new ProjectException(GeneralErrorCode.BAD_REQUEST,
                    "AI 서버 분석 실패: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("[AI Client] [실제 AI 서버 통신 실패] AI 서버와 통신 중 에러가 발생했습니다. 에러 메시지: {}", e.getMessage(), e);
            throw new ProjectException(GeneralErrorCode.INTERNAL_SERVER_ERROR,
                    "AI 서버 연결 실패: " + e.getMessage());
        }
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
