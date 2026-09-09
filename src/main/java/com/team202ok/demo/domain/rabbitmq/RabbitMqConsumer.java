package com.team202ok.demo.domain.rabbitmq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team202ok.demo.domain.ai.service.AiAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.rabbitmq.enabled", havingValue = "true")
public class RabbitMqConsumer {
    private final AnalysisJobTransitions transitions;
    private final AiAnalysisService analysis;
    private final ObjectMapper mapper;

    @RabbitListener(queues = RabbitMqConfig.QUEUE, concurrency = "1")
    public void consume(String jobId) {
        AnalysisJob job = transitions.claim(jobId);
        if (job == null) return;
        var claimTime = job.getUpdatedAt();
        long started = System.nanoTime();
        log.info("AI job started: jobId={}, queueWaitMs={}", jobId,
                java.time.Duration.between(job.getCreatedAt(), claimTime).toMillis());
        String resultJson = null;
        String error = null;
        try {
            var result = analysis.analyze(new StoredImage(job.getFilename(), job.getContentType(), job.getImage()), job.getUserId());
            resultJson = mapper.writeValueAsString(result);
        } catch (Exception e) {
            log.error("AI job failed: {}", jobId, e);
            error = "AI 분석에 실패했습니다. 서버 연결 상태를 확인한 후 다시 요청해 주세요.";
        }
        transitions.finish(jobId, claimTime, resultJson, error);
        log.info("AI job finished: jobId={}, status={}, processingMs={}", jobId,
                error == null ? "COMPLETED" : "FAILED", (System.nanoTime() - started) / 1_000_000);
    }
}
