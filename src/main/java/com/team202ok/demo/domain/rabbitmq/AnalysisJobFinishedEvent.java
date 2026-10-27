package com.team202ok.demo.domain.rabbitmq;

public record AnalysisJobFinishedEvent(String jobId, Long userId, String status) {
}
