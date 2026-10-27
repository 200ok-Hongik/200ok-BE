package com.team202ok.demo.domain.ai.sse;

import com.team202ok.demo.domain.rabbitmq.AnalysisJobService;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import org.junit.jupiter.api.Test;

class AnalysisJobSseServiceTest {
    private final AnalysisJobService jobs = mock(AnalysisJobService.class);
    private final AnalysisJobSseService service = new AnalysisJobSseService(jobs);

    @Test
    void keepsSubscriptionForRunningJobAndRemovesItAfterCompletion() throws IOException {
        when(jobs.get("job", 1L))
                .thenReturn(new AnalysisJobService.JobView("job", "PROCESSING", null, null));

        service.subscribe("job", 1L);

        assertThat(service.subscriberCount("job")).isEqualTo(1);
        service.sendTerminal("job", "COMPLETED");
        assertThat(service.subscriberCount("job")).isZero();
    }

    @Test
    void immediatelyCompletesSubscriptionWhenJobAlreadyFinished() throws IOException {
        when(jobs.get("job", 1L))
                .thenReturn(new AnalysisJobService.JobView("job", "COMPLETED", null, null));

        service.subscribe("job", 1L);

        assertThat(service.subscriberCount("job")).isZero();
    }
}
