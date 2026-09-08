package com.team202ok.demo.domain.rabbitmq;

import jakarta.validation.constraints.NotBlank;

public record RabbitMqTestRequest(
        @NotBlank String event,
        Object body
) {
}
