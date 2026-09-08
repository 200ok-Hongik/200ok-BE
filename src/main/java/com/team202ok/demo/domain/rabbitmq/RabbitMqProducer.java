package com.team202ok.demo.domain.rabbitmq;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.rabbitmq.enabled", havingValue = "true")
public class RabbitMqProducer {
    private final RabbitTemplate rabbitTemplate;
    public void send(String jobId) {
        CorrelationData correlation = new CorrelationData(jobId);
        rabbitTemplate.convertAndSend(RabbitMqConfig.EXCHANGE, RabbitMqConfig.ROUTING_KEY, jobId, correlation);
        try {
            var confirm = correlation.getFuture().get(10, TimeUnit.SECONDS);
            if (!confirm.isAck() || correlation.getReturned() != null) {
                throw new IllegalStateException("RabbitMQ did not accept analysis job");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("RabbitMQ publish interrupted", e);
        } catch (Exception e) {
            throw new IllegalStateException("RabbitMQ publish failed", e);
        }
    }
}
