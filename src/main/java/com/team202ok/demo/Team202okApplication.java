package com.team202ok.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.context.annotation.Bean;
import org.springframework.data.auditing.DateTimeProvider;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

@EnableJpaAuditing(dateTimeProviderRef = "auditDateTimeProvider")
@EnableScheduling
@SpringBootApplication
public class Team202okApplication {

	@Bean
	DateTimeProvider auditDateTimeProvider() {
		return () -> Optional.of(LocalDateTime.now(ZoneId.of("Asia/Seoul")));
	}

	public static void main(String[] args) {
		SpringApplication.run(Team202okApplication.class, args);
	}

}
