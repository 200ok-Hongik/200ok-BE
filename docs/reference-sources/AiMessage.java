package com.team202ok.demo.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

public record AiMessage<T>(Header hd, T bd) {

    public static <T> AiMessage<T> of(String event, T body) {
        return new AiMessage<>(
                new Header(UUID.randomUUID().toString(), event, Instant.now(), 1),
                body
        );
    }

    public record Header(
            @JsonProperty("request_id") String requestId,
            String event,
            @JsonProperty("occurred_at") Instant occurredAt,
            @JsonProperty("schema_version") int schemaVersion
    ) {
        public Header(String event) {
            this(UUID.randomUUID().toString(), event, Instant.now(), 1);
        }
    }
}
