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

        String stage = "HTTP_REQUEST";
        String trace = org.slf4j.MDC.get("aiJobId");
        if (trace == null) trace = java.util.UUID.randomUUID().toString();
        log.info("[AI Client] trace={}, imageBytes={}, contentType={}, timeoutSeconds={}", trace, image.getSize(), image.getContentType(), ANALYSIS_TIMEOUT.toSeconds());
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
                    .toEntity(String.class)
                    .doOnNext(entity -> log.info("[AI Client] HTTP response: status={}, contentType={}", entity.getStatusCode().value(), entity.getHeaders().getContentType()))
                    .map(entity -> entity.getBody() == null ? "" : entity.getBody())
                    .timeout(ANALYSIS_TIMEOUT)
                    .block();

            log.info("[AI Client] trace={}, responseReceivedMs={}", trace, (System.nanoTime() - started) / 1_000_000);
            logRawResponse(trace, rawResponse);
            stage = "JSON_PARSE";
            AiModelResponse response = objectMapper.readValue(rawResponse, AiModelResponse.class);
            stage = "V1_VALIDATION";
            if (response == null) throw new IllegalArgumentException("response: null JSON document");
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
            log.error("[AI Client] trace={}, stage={}, elapsedMs={}, exception={}, reason={}", trace, stage, (System.nanoTime() - started) / 1_000_000, e.getClass().getName(), e.getMessage(), e);
            throw new ProjectException(GeneralErrorCode.INTERNAL_SERVER_ERROR,
                    "AI 처리 실패 [" + stage + "]: " + e.getMessage());
        }
    }

    private void logRawResponse(String trace, String raw) throws com.fasterxml.jackson.core.JsonProcessingException {
        // Escape line breaks so upstream text cannot forge log entries. Split to avoid line truncation.
        String escaped = objectMapper.writeValueAsString(raw);
        int chunkSize = 2000;
        int parts = Math.max(1, (escaped.length() + chunkSize - 1) / chunkSize);
        for (int i = 0; i < parts; i++) {
            log.info("[AI RAW] trace={}, part={}/{}, jsonEscaped={}", trace, i + 1, parts,
                    escaped.substring(i * chunkSize, Math.min(escaped.length(), (i + 1) * chunkSize)));
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
