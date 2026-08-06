package com.team202ok.demo.global;

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
                        //"https://playbit.vercel.app", // 프론트 실제 배포 주소
                        "http://localhost:3000",      // 로컬 테스트용
                        "http://localhost:5173",      // 로컬 테스트용(Vite)
                        "https://essential-family-display.ngrok-free.dev" //ngrok
                )
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("Content-Type", "Authorization", "ngrok-skip-browser-warning")
                .allowCredentials(true);
    }
}