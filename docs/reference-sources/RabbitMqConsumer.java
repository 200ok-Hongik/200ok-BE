package com.team202ok.demo.domain;

import com.example.kkikki_be_server.batch.service.TrendAnalysisResultService;
import com.example.kkikki_be_server.infra.message.rabbitmq.dto.TrendAnalysisResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class RabbitMqConsumer {
	private static final Logger log = LoggerFactory.getLogger(RabbitMqConsumer.class);
	private static final String RESULT_EVENT = "trend_analyze_result";

	private final TrendAnalysisResultService trendAnalysisResultService;

	public RabbitMqConsumer(TrendAnalysisResultService trendAnalysisResultService) {
		this.trendAnalysisResultService = trendAnalysisResultService;
	}

	public void consume(AiMessage<TrendAnalysisResult> message) {
		if (message == null || message.hd() == null || !RESULT_EVENT.equals(message.hd().event())) {
			throw new IllegalArgumentException("지원하지 않는 AI 응답 이벤트입니다.");
		}
		log.info("[RabbitMQ] AI result consumed: requestId={}", message.hd().requestId());
		trendAnalysisResultService.apply(message.hd().requestId(), message.bd());
	}
}
