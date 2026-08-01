package com.flydeer.structmind.controller.utils;

import com.flydeer.structmind.contract.user.vo.JwtTokenVO;
import com.flydeer.structmind.service.user.config.JwtTokenConfig;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class AuthCookieUtils {

    private final JwtTokenConfig jwtTokenConfig;

    public void writeRefreshCookie(HttpServletResponse response, JwtTokenVO tokens) {
        ResponseCookie cookie = ResponseCookie.from(jwtTokenConfig.getRefreshCookieName(), tokens.getRefreshToken())
            .httpOnly(true)
            .secure(jwtTokenConfig.getRefreshCookieSecure())
            .path(jwtTokenConfig.getRefreshCookiePath())
            .sameSite("Lax")
            .maxAge(jwtTokenConfig.getRefreshTokenTtl())
            .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public void clearRefreshCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(jwtTokenConfig.getRefreshCookieName(), "")
            .httpOnly(true)
            .secure(jwtTokenConfig.getRefreshCookieSecure())
            .path(jwtTokenConfig.getRefreshCookiePath())
            .sameSite("Lax")
            .maxAge(0)
            .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public String readRefreshCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        String name = jwtTokenConfig.getRefreshCookieName();
        for (Cookie cookie : cookies) {
            if (name.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    public String extractAccessToken(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            return null;
        }
        return header.substring("Bearer ".length()).trim();
    }

    public String getRedirectUrl() {
        return jwtTokenConfig.getRedirectUrl();
    }
}
