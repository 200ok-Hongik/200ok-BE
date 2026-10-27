package com.team202ok.demo.domain.ai.sse;

import com.team202ok.demo.domain.rabbitmq.AnalysisJobFinishedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AnalysisJobFinishedListener {
    private final AnalysisJobSseService jobEvents;

    @EventListener
    public void onFinished(AnalysisJobFinishedEvent event) {
        jobEvents.sendTerminal(event.jobId(), event.status());
    }
}
