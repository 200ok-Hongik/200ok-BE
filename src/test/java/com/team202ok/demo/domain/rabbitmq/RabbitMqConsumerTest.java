package com.team202ok.demo.domain.rabbitmq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team202ok.demo.domain.ai.dto.AiRes;
import com.team202ok.demo.domain.ai.service.AiAnalysisService;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

class RabbitMqConsumerTest {
    private final AnalysisJobRepository jobs = mock(AnalysisJobRepository.class);
    private final AiAnalysisService ai = mock(AiAnalysisService.class);
    private final RabbitMqConsumer consumer = new RabbitMqConsumer(new AnalysisJobTransitions(jobs), ai, new ObjectMapper());
    @Test void completesAndIgnoresDuplicateDelivery() {
        var job = new AnalysisJob("job", 1L, "test.png", "image/png", new byte[]{1});
        when(jobs.findLocked("job")).thenReturn(Optional.of(job));
        when(ai.analyze(any(), eq(1L))).thenReturn(new AiRes.Analyze(2L, List.of(), List.of()));
        consumer.consume("job");
        consumer.consume("job");
        verify(ai, times(1)).analyze(any(), eq(1L));
        assertThat(job.getStatus()).isEqualTo("COMPLETED");
        assertThat(job.getResultJson()).contains("scanResultId");
        assertThat(job.getImage()).isNull();
    }
    @Test void recordsFailureAndDoesNotRetryInvalidAnalysisForever() {
        var job = new AnalysisJob("job", 1L, "test.png", "image/png", new byte[]{1});
        when(jobs.findLocked("job")).thenReturn(Optional.of(job));
        when(ai.analyze(any(), eq(1L))).thenThrow(new IllegalStateException("upstream failed"));
        consumer.consume("job");
        consumer.consume("job");
        assertThat(job.getStatus()).isEqualTo("FAILED");
        assertThat(job.getImage()).isNull();
        verify(ai, times(1)).analyze(any(), eq(1L));
    }
}
