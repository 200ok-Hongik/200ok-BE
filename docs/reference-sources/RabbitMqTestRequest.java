package com.team202ok.demo.domain;

import jakarta.validation.constraints.NotBlank;

public record RabbitMqTestRequest(
        @NotBlank String event,
        Object body
) {
}
