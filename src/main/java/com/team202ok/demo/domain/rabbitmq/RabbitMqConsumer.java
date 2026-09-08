package com.team202ok.demo.domain.rabbitmq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team202ok.demo.domain.ai.service.AiAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.rabbitmq.enabled", havingValue = "true")
public class RabbitMqConsumer {
    private final AnalysisJobRepository jobs;
    private final AiAnalysisService analysis;
    private final ObjectMapper mapper;

    @RabbitListener(queues = RabbitMqConfig.QUEUE, concurrency = "1")
    @Transactional
    public void consume(String jobId) {
        AnalysisJob job = jobs.findLocked(jobId).orElse(null);
        // Serializes duplicate deliveries; completed/failed jobs are never processed again.
        if (job == null || !"QUEUED".equals(job.getStatus())) return;
        job.processing();
        try {
            var result = analysis.analyze(new StoredImage(job.getFilename(), job.getContentType(), job.getImage()), job.getUserId());
            job.complete(mapper.writeValueAsString(result));
        } catch (Exception e) {
            log.error("AI job failed: {}", jobId, e);
            job.fail("AI 분석에 실패했습니다. 서버 연결 상태를 확인한 후 다시 요청해 주세요.");
        }
        jobs.save(job);
    }
}
