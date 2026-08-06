package com.team202ok.demo.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class AiModelWebClient {

    @Value("${ai.server.url}")
    private String aiServerUrl;

    @Bean
    public WebClient aiServerWebClient() {
        return WebClient.builder()
                .baseUrl(aiServerUrl)
                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
