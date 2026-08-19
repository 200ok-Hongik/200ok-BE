package com.team202ok.demo.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry){
        registry.addMapping("/**")
                .allowedOrigins(
                        "https://ssok.store",
                        "https://dev.ssok.store",
                        "http://localhost:3000",      // 로컬 테스트용
                        "http://localhost:5173",      // 로컬 테스트용(Vite)
                        "https://essential-family-display.ngrok-free.dev" //ngrok
                )
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("Content-Type", "Authorization", "ngrok-skip-browser-warning")
                .allowCredentials(true);
    }
}
