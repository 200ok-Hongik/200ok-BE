package com.team202ok.demo.recycle.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI swagger() {
        Info info = new Info()
                .title("200OK SSOK API")
                .description("200OK SSOK API 명세서")
                .version("0.0.1");

        // JWT 토큰 헤더 방식 (사용 안 함 - 주석 처리)
        // String securityScheme = "JWT TOKEN";
        // SecurityRequirement securityRequirement = new SecurityRequirement().addList(securityScheme);

        // Components components = new Components()
        //         .addSecuritySchemes(securityScheme, new SecurityScheme()
        //                 .name(securityScheme)
        //                 .type(SecurityScheme.Type.HTTP)
        //                 .scheme("Bearer")
        //                 .bearerFormat("JWT"));

        return new OpenAPI()
                .info(info)
                .addServersItem(new Server().url("/"));
        // .addSecurityItem(securityRequirement)
        // .components(components);
    }
}