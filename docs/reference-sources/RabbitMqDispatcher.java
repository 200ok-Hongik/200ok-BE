package com.team202ok.demo.domain;

import com.example.kkikki_be_server.infra.message.rabbitmq.dto.TrendAnalysisResult;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class RabbitMqDispatcher {
	private final RabbitMqConsumer rabbitMqConsumer;
	private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

	public RabbitMqDispatcher(RabbitMqConsumer rabbitMqConsumer) {
		this.rabbitMqConsumer = rabbitMqConsumer;
	}

	public void dispatch(Object message) {
		AiMessage<TrendAnalysisResult> resultMessage = objectMapper.convertValue(
				message,
				new TypeReference<>() {
				}
		);
		rabbitMqConsumer.consume(resultMessage);
	}
}
