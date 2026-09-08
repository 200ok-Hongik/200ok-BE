package com.team202ok.demo.domain.rabbitmq;

import com.fasterxml.jackson.databind.*;
import com.team202ok.demo.global.exception.code.GeneralErrorCode;
import com.team202ok.demo.global.exception.custom.ProjectException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AnalysisJobService {
    private final AnalysisJobRepository jobs;
    @Value("${app.rabbitmq.enabled:false}")
    private boolean enabled;
    private final ObjectMapper mapper;

    public JobView submit(MultipartFile image, Long userId) throws IOException {
        if (!enabled) throw new ProjectException(
                GeneralErrorCode.SERVICE_UNAVAILABLE, "비동기 분석 큐가 활성화되지 않았습니다.");
        if (userId == null) throw new ProjectException(GeneralErrorCode.FORBIDDEN);
        if (image.isEmpty() || image.getSize() > 10 * 1024 * 1024
                || image.getContentType() == null || !image.getContentType().startsWith("image/")) {
            throw new ProjectException(GeneralErrorCode.BAD_REQUEST, "10MB 이하 이미지를 보내 주세요.");
        }
        AnalysisJob job = jobs.save(new AnalysisJob(UUID.randomUUID().toString(), userId,
                image.getOriginalFilename(), image.getContentType(), image.getBytes()));
        // Durable handoff: the relay publishes after this request returns, without
        // waiting for RabbitMQ or AI. Pending work survives a broker outage.
        return new JobView(job.getId(), "QUEUED", null, null);
    }

    public JobView get(String jobId, Long userId) throws IOException {
        AnalysisJob job = jobs.findByIdAndUserId(jobId, userId)
                .orElseThrow(() -> new ProjectException(GeneralErrorCode.NOT_FOUND));
        return new JobView(job.getId(), job.getStatus(),
                job.getResultJson() == null ? null : mapper.readTree(job.getResultJson()), job.getErrorMessage());
    }
    public record JobView(String jobId, String status, JsonNode result, String errorMessage) {}
}
