package com.team202ok.demo.global.config;

import com.team202ok.demo.global.auth.handler.OAuth2LoginSuccessHandler;
import com.team202ok.demo.global.auth.jwt.JwtAuthenticationFilter;
import com.team202ok.demo.global.auth.jwt.JwtProperties;
import com.team202ok.demo.global.auth.jwt.JwtTokenProvider;
import com.team202ok.demo.global.auth.service.AuthCookieService;
import com.team202ok.demo.global.auth.service.KakaoOAuth2UserService;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.Customizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(JwtProperties.class)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final KakaoOAuth2UserService kakaoOAuth2UserService;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final AuthCookieService authCookieService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/health",
                                "/api/auth/**",
                                "/api/ai/server/health",
                                "/api/regions/**",
                                "/api/trash-categories/**",
                                "/oauth2/**", "/login/oauth2/**",
                                "/swagger-ui/**", "/v3/api-docs/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(errors -> errors.defaultAuthenticationEntryPointFor(
                        new org.springframework.security.web.authentication.HttpStatusEntryPoint(org.springframework.http.HttpStatus.UNAUTHORIZED),
                        new org.springframework.security.web.util.matcher.AntPathRequestMatcher("/api/**")
                ))
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .oauth2Login(oauth -> oauth
                        .userInfoEndpoint(userInfo -> userInfo.userService(kakaoOAuth2UserService))
                        .successHandler(oAuth2LoginSuccessHandler)
                )
                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtTokenProvider, authCookieService),
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}
