package com.team202ok.demo.domain.rabbitmq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.Instant;

/** Republishes durable pending jobs after broker outages or a crash before publish. */
@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.rabbitmq.enabled", havingValue = "true")
public class AnalysisJobRelay {
    private final AnalysisJobRepository jobs;
    private final RabbitMqProducer producer;
    private final AnalysisJobTransitions transitions;
    @Scheduled(fixedDelayString = "${app.rabbitmq.relay-delay-ms:1000}")
    public void relay() {
        for (String id : jobs.findPendingIds(Instant.now().minusSeconds(AnalysisJobTransitions.RECOVERY_SECONDS), PageRequest.of(0, 20))) {
            try { if (transitions.reserveDispatch(id)) producer.send(id); }
            catch (IllegalStateException e) { log.warn("RabbitMQ unavailable; pending jobs will be retried"); break; }
        }
    }
}
