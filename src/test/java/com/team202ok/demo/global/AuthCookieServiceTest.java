package com.team202ok.demo.global;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class AuthCookieServiceTest {

    private final AuthCookieService authCookieService = new AuthCookieService(
            new JwtProperties("test-secret", new JwtProperties.Expiration(1_800_000, 1_209_600_000))
    );

    @Test
    void accessTokenCookieIsSecureAndHttpOnly() {
        MockHttpServletResponse response = new MockHttpServletResponse();

        authCookieService.addAccessToken(response, "access-token");

        String setCookie = response.getHeader("Set-Cookie");
        assertThat(setCookie)
                .contains("accessToken=access-token")
                .contains("Path=/")
                .contains("Secure")
                .contains("HttpOnly")
                .contains("SameSite=Strict");
    }

    @Test
    void resolvesTokensFromRequestCookies() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie(AuthCookieService.ACCESS_TOKEN_COOKIE, "access-token"));

        assertThat(authCookieService.resolveAccessToken(request)).isEqualTo("access-token");
    }

    @Test
    void clearExpiresBothCookies() {
        MockHttpServletResponse response = new MockHttpServletResponse();

        authCookieService.clear(response);

        assertThat(response.getHeaders("Set-Cookie"))
                .hasSize(2)
                .allMatch(cookie -> cookie.contains("Max-Age=0"));
    }
}
