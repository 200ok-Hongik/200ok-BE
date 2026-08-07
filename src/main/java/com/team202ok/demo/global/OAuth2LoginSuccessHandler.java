package com.team202ok.demo.global;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final AuthCookieService authCookieService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        OAuth2User principal = (OAuth2User) authentication.getPrincipal();
        Long userId = principal.getAttribute("userId");

        authCookieService.addAccessToken(response, jwtTokenProvider.createAccessToken(userId));
        authCookieService.addRefreshToken(response, jwtTokenProvider.createRefreshToken(userId));

        SecurityContextHolder.clearContext();
        if (request.getSession(false) != null) {
            request.getSession(false).invalidate();
        }
        response.sendRedirect(frontendUrl + "/oauth/callback");
    }
}
