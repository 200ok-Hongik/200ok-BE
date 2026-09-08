package com.team202ok.demo.domain.rabbitmq;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
@ConditionalOnProperty(name = "app.rabbitmq.enabled", havingValue = "true")
public class RabbitMqConfig {
    public static final String EXCHANGE = "recycle.ai";
    public static final String QUEUE = "recycle.ai.analyze";
    public static final String ROUTING_KEY = "analyze";
    @Bean DirectExchange aiExchange() { return new DirectExchange(EXCHANGE, true, false); }
    @Bean Queue aiQueue() { return QueueBuilder.durable(QUEUE).build(); }
    @Bean Binding aiBinding() { return BindingBuilder.bind(aiQueue()).to(aiExchange()).with(ROUTING_KEY); }
    @Bean Jackson2JsonMessageConverter rabbitMessageConverter(ObjectMapper mapper) {
        return new Jackson2JsonMessageConverter(mapper);
    }
}
