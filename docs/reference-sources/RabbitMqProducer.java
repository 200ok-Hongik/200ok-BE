package com.team202ok.demo.domain;

import com.example.kkikki_be_server.global.config.RabbitMqConfig.RabbitMqEndpoint;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class RabbitMqProducer {
	private final RabbitTemplate rabbitTemplate;
	private final RabbitMqEndpoint rabbitMqEndpoint;

	public RabbitMqProducer(RabbitTemplate rabbitTemplate, RabbitMqEndpoint rabbitMqEndpoint) {
		this.rabbitTemplate = rabbitTemplate;
		this.rabbitMqEndpoint = rabbitMqEndpoint;
	}

	public void send(Object message) {
		rabbitTemplate.convertAndSend(
			rabbitMqEndpoint.exchange(),
			rabbitMqEndpoint.requestRoutingKey(),
			message
		);
	}
}
