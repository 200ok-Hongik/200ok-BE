package com.team202ok.demo.domain.ai.sse;

import com.team202ok.demo.domain.rabbitmq.AnalysisJobService;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalysisJobSseService {
    private final AnalysisJobService jobs;
    private final ConcurrentHashMap<String, Set<SseEmitter>> emitters = new ConcurrentHashMap<>();

    @Value("${app.sse.analysis-timeout-ms:600000}")
    private long timeoutMs;

    public SseEmitter subscribe(String jobId, Long userId) throws IOException {
        AnalysisJobService.JobView before = jobs.get(jobId, userId);
        SseEmitter emitter = new SseEmitter(timeoutMs);
        emitters.computeIfAbsent(jobId, ignored -> ConcurrentHashMap.newKeySet()).add(emitter);
        emitter.onCompletion(() -> remove(jobId, emitter));
        emitter.onTimeout(() -> remove(jobId, emitter));
        emitter.onError(error -> remove(jobId, emitter));

        try {
            emitter.send(SseEmitter.event().name("connected")
                    .data(new JobEvent(jobId, before.status())));
            // The job can finish between the ownership check and emitter registration.
            AnalysisJobService.JobView after = jobs.get(jobId, userId);
            if (terminal(after.status())) {
                sendTerminal(jobId, after.status());
            }
        } catch (IOException | RuntimeException exception) {
            remove(jobId, emitter);
            throw exception;
        }
        return emitter;
    }

    public void sendTerminal(String jobId, String status) {
        Set<SseEmitter> subscribers = emitters.remove(jobId);
        if (subscribers == null) return;

        String eventName = "COMPLETED".equals(status) ? "completed" : "failed";
        for (SseEmitter emitter : subscribers) {
            try {
                emitter.send(SseEmitter.event().name(eventName)
                        .data(new JobEvent(jobId, status)));
                emitter.complete();
            } catch (IOException | RuntimeException exception) {
                log.debug("SSE delivery failed: jobId={}, status={}", jobId, status, exception);
                emitter.completeWithError(exception);
            }
        }
    }

    int subscriberCount(String jobId) {
        Set<SseEmitter> subscribers = emitters.get(jobId);
        return subscribers == null ? 0 : subscribers.size();
    }

    private void remove(String jobId, SseEmitter emitter) {
        emitters.computeIfPresent(jobId, (ignored, subscribers) -> {
            subscribers.remove(emitter);
            return subscribers.isEmpty() ? null : subscribers;
        });
    }

    private boolean terminal(String status) {
        return "COMPLETED".equals(status) || "FAILED".equals(status);
    }

    public record JobEvent(String jobId, String status) {
    }
}
