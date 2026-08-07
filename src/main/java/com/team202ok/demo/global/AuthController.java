package com.team202ok.demo.global;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtTokenProvider jwtTokenProvider;
    private final AuthCookieService authCookieService;

    @PostMapping("/refresh")
    public ResponseEntity<Void> refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = authCookieService.resolveRefreshToken(request);
        if (refreshToken == null
                || !jwtTokenProvider.validateToken(refreshToken)
                || !jwtTokenProvider.isRefreshToken(refreshToken)) {
            return ResponseEntity.status(401).build();
        }

        Long userId = jwtTokenProvider.getUserId(refreshToken);
        authCookieService.addAccessToken(response, jwtTokenProvider.createAccessToken(userId));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        authCookieService.clear(response);
        return ResponseEntity.noContent().build();
    }
}
