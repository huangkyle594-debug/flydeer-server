package com.flydeer.structmind.controller.support;

import com.flydeer.structmind.service.utils.JwtTokenUtils;
import com.flydeer.structmind.service.config.AppAuthProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class AuthCookieSupport {

    private final AppAuthProperties properties;

    public AuthCookieSupport(AppAuthProperties properties) {
        this.properties = properties;
    }

    public void writeRefreshCookie(HttpServletResponse response, JwtTokenUtils.IssuedTokens tokens) {
        ResponseCookie cookie = ResponseCookie.from(
                        properties.getAuth().getRefreshCookieName(), tokens.refreshToken())
                .httpOnly(true)
                .secure(properties.getAuth().isRefreshCookieSecure())
                .path(properties.getAuth().getRefreshCookiePath())
                .sameSite("Lax")
                .maxAge(properties.getAuth().getRefreshTokenTtl())
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public void clearRefreshCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(properties.getAuth().getRefreshCookieName(), "")
                .httpOnly(true)
                .secure(properties.getAuth().isRefreshCookieSecure())
                .path(properties.getAuth().getRefreshCookiePath())
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
        String name = properties.getAuth().getRefreshCookieName();
        for (Cookie cookie : cookies) {
            if (name.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    public String extractBearer(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            return null;
        }
        return header.substring("Bearer ".length()).trim();
    }
}
